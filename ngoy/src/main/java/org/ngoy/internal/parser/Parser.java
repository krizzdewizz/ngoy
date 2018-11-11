package org.ngoy.internal.parser;

import static java.lang.String.format;
import static java.util.Arrays.asList;
import static java.util.Collections.emptySet;
import static java.util.stream.Collectors.toList;
import static org.ngoy.core.NgoyException.wrap;
import static org.ngoy.internal.parser.Inputs.cmpInputs;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.Jsoup;
import org.jsoup.nodes.CDataNode;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.NodeVisitor;
import org.ngoy.core.HostBinding;
import org.ngoy.core.NgoyException;
import org.ngoy.core.Nullable;
import org.ngoy.core.OnCompile;
import org.ngoy.core.internal.CmpRef;
import org.ngoy.core.internal.ContainerComponent;
import org.ngoy.core.internal.JSoupElementRef;
import org.ngoy.core.internal.Resolver;
import org.ngoy.internal.parser.visitor.SkipSubTreeVisitor;

public class Parser {

	public static Document parseHtml(String template) {
		try {
			return Jsoup.parse(template, "");
		} catch (Exception e) {
			throw wrap(e);
		}
	}

	private List<Node> parseBody(String template, boolean forceHtmlContentType) {
		try {
			if (!forceHtmlContentType && "text/plain".equals(contentType)) {
				template = format("<![CDATA[%s]]>", template);
			}
			Document doc = Jsoup.parseBodyFragment(template);
			return doc.body()
					.childNodes();
		} catch (Exception e) {
			throw wrap(e);
		}
	}

	private class Visitor implements NodeVisitor {
		@Override
		public void head(Node node, int depth) {
			replaceExprs(node);
		}

		@Override
		public void tail(Node node, int depth) {
			if (node instanceof Element) {
				endElement((Element) node);
			}
		}
	}

	private static final String BINDING_CLASS = "class.";
	private static final String BINDING_ATTR = "attr.";

	private static final Pattern PIPE_PARAM_PATTERN = Pattern.compile("(.*?):(.*)");
	private static final Pattern EXPR_PATTERN = Pattern.compile("\\{\\{(.*?)\\}\\}", Pattern.MULTILINE);
	private static final Pattern PIPE_PATTERN = Pattern.compile("([^\\|]+)");
	private static final Pattern NG_CONTAINER_PATTERN = Pattern.compile("<ng-container(.*)>((.|\\s)*)</ng-container>", Pattern.MULTILINE);
	private static final Pattern NG_FOR_PATTERN = Pattern.compile("\\*ngFor=\"(.*)\"");

	private final LinkedList<Element> elementConditionals = new LinkedList<>();
	private final LinkedList<Element> elementRepeated = new LinkedList<>();
	private final Set<Element> cmpElements = new HashSet<>();
	public boolean parseBody;
	public boolean inlineComponents;
	public String contentType;

	private MyHandler handler;
	private Resolver resolver;
	private SkipSubTreeVisitor visitor;

	private static class MyHandler extends ParserHandler.Delegate {
		final LinkedList<String> cmpClassesStack = new LinkedList<>();

		public MyHandler(ParserHandler target) {
			super(target);
		}

		@Override
		public void componentStart(String clazz, List<String> params) {
			super.componentStart(clazz, params);
			cmpClassesStack.push(clazz);
		}

		@Override
		public void componentEnd() {
			super.componentEnd();
			cmpClassesStack.pop();
		}
	}

	public Parser() {
		this(null);
	}

	/**
	 * @param resolver if null, uses {@link Resolver#DEFAULT}
	 */
	public Parser(@Nullable Resolver resolver) {
		this.resolver = resolver == null ? Resolver.DEFAULT : resolver;
	}

	public void parse(String template, ParserHandler handler) {

		Objects.requireNonNull(template, "template must not be null.");

		this.handler = new MyHandler(handler);
		visitor = new SkipSubTreeVisitor(new Visitor());
		List<Node> nodes = parseBody ? parseBody(template, false) : asList(parseHtml(template));

		acceptDocument(nodes);
	}

	private void acceptDocument(List<Node> nodes) {
		this.handler.documentStart();
		accept(nodes);
		this.handler.documentEnd();
	}

	private void replaceExprs(Node node) {
		if (node instanceof CDataNode) {
			replaceCData(node);
		} else if (node instanceof TextNode) {
			replaceExpr(((TextNode) node).text());
		} else if (node instanceof Element) {
			replaceElement(node, true);
		}
	}

	private void replaceCData(Node node) {
		String text = ((CDataNode) node).text();

		Element c = node.ownerDocument()
				.createElement("ng-container");
		String content;
		Matcher matcher = NG_CONTAINER_PATTERN.matcher(text);
		if (matcher.find()) {
			content = matcher.group(2);
			Matcher forMatcher = NG_FOR_PATTERN.matcher(matcher.group(1));
			if (forMatcher.find()) {
				c.attr("*ngFor", forMatcher.group(1));
			}
		} else {
			content = text;
		}

		replaceElement(c, false);
		replaceExpr(content);

		handler.componentEnd();
		handler.ngContentEnd();
		visitor.tail(c, -1);
	}

	private void replaceElement(Node node, boolean acceptChildren) {
		Element el = (Element) node;

		String ngIf = el.attr("*ngIf");
		if (!ngIf.isEmpty()) {
			elementConditionals.push(el);

			int pos = ngIf.lastIndexOf(";");
			if (pos >= 0) {
				ngIf = ngIf.substring(0, pos);
			}

			handler.elementConditionalStart(ngIf);
		}

		String ngFor = el.attr("*ngFor");
		if (!ngFor.isEmpty()) {
			elementRepeated.push(el);
			handler.elementRepeatedStart(ngFor);
		}

		JSoupElementRef domEl = new JSoupElementRef(el);
		List<CmpRef> cmpRefs = resolver.resolveCmps(domEl);
		compileDirectives(cmpRefs, el);

		if (cmpRefs.isEmpty()) {
			handler.elementHead(el.nodeName());
			replaceAttrExpr(el, emptySet(), null, true);
			handler.elementHeadEnd();

			acceptSpecialElementsContent(el);
		} else {

			boolean directive = checkAllForDirective(el, cmpRefs);
			int countCmpRefs = cmpRefs.size();
			Set<String> excludeBindings = new HashSet<>();

			if (directive) {
				handler.elementHead(el.nodeName());
				for (int i = 0; i < countCmpRefs; i++) {
					CmpRef cmpRef = cmpRefs.get(i);
					handler.componentStart(cmpRef.clazz.getName(), cmpInputs(domEl, cmpRef.clazz, excludeBindings));
					replaceAttrExpr(el, excludeBindings, cmpRef.clazz, i == 0);
				}
				handler.elementHeadEnd();
				for (int i = 0; i < countCmpRefs; i++) {
					handler.componentEnd();
				}
			} else {
				CmpRef cmpRef = cmpRefs.get(0);
				handler.componentStart(cmpRef.clazz.getName(), cmpInputs(domEl, cmpRef.clazz, excludeBindings));

				if (inlineComponent(el)) {
					cmpElements.add(el);
				} else {
					handler.elementHead(el.nodeName());
					replaceAttrExpr(el, excludeBindings, cmpRef.clazz, true);
					handler.elementHeadEnd();
				}

				acceptCmpRef(el, cmpRef, acceptChildren);

				if (acceptChildren) {
					handler.componentEnd();
				}

				visitor.skipSubTree(el);
			}
		}
	}

	private void acceptCmpRef(Element el, CmpRef cmpRef, boolean acceptChildren) {
		List<Node> cmpNodes = parseBody(cmpRef.template, true);

		Element ngContentEl = findNgContent(cmpNodes);

		if (ngContentEl == null) {
			accept(cmpNodes);
			return;
		}

		boolean invokeHandler = !ngContentEl.hasAttr("scope");
		String select = ngContentEl.attr("select");
		String selector = select.isEmpty() ? ngContentEl.attr("selector") : select;

		Element parent = ngContentEl.parent();

		List<Node> childNodes = new ArrayList<>(parent.childNodes());
		int ngContentIndex = childNodes.indexOf(ngContentEl);

		List<Node> nodesBefore = childNodes.subList(0, ngContentIndex);
		List<Node> nodesAfter = childNodes.subList(ngContentIndex + 1, childNodes.size());

		ngContentEl.remove();

		if (acceptChildren) {
			accept(nodesBefore);
		}

		if (invokeHandler) {
			handler.ngContentStart();
		}

		if (acceptChildren) {
			if (selector.isEmpty()) {
				accept(el.childNodes());
			} else {
				el.childNodes()
						.stream()
						.filter(Element.class::isInstance)
						.map(Element.class::cast)
						.filter(e -> e.is(selector))
						.forEach(e -> e.traverse(visitor));
			}

			if (invokeHandler) {
				handler.ngContentEnd();
			}

			accept(nodesAfter);
		}

	}

	private void accept(List<Node> nodes) {
		nodes.forEach(n -> n.traverse(visitor));
	}

	@Nullable
	private Element findNgContent(List<Node> cmpNodes) {
		return cmpNodes.stream()
				.filter(Element.class::isInstance)
				.map(Element.class::cast)
				.map(cmpNode -> cmpNode.selectFirst("ng-content"))
				.filter(Objects::nonNull)
				.findFirst()
				.orElse(null);
	}

	private boolean checkAllForDirective(Element el, List<CmpRef> cmpRefs) {
		if (cmpRefs.size() > 1) {
			for (CmpRef ref : cmpRefs) {
				if (!ref.directive) {
					throw new NgoyException("More than one component found for element '%s'", el.cssSelector());
				}
			}
			return true;
		}

		return cmpRefs.get(0).directive;
	}

	private void acceptSpecialElementsContent(Element el) {
		String nodeName = el.nodeName();
		if (nodeName.equals("script")) {
			el.childNodes()
					.forEach(c -> handler.text(c.toString(), false, null));
		}
	}

	private Element findTemplate(String ref, Document document) {
		Element template = document.selectFirst(format("ng-template[#%s]", ref));
		if (template == null) {
			throw new ParseException("no <ng-template> found for name %s", ref);
		}

		return template;
	}

	private void replaceAttrExpr(Element el, Set<String> excludeBindings, @Nullable Class<?> cmpClass, boolean first) {

		// add lower case copy because jsoup attributes are all lower case
		new HashSet<String>(excludeBindings).stream()
				.map(String::toLowerCase)
				.forEach(excludeBindings::add);

		List<String[]> classNames = first ? el.classNames()
				.stream()
				.map(it -> new String[] { it, "" })
				.collect(toList()) : new ArrayList<>();

		List<String[]> attrNames = new ArrayList<>();

		if (first) {
			el.attributes()
					.asList()
					.stream()
					.filter(attr -> !attr.getKey()
							.equals("class")
							&& !attr.getKey()
									.startsWith("*"))
					.forEach(attr -> {
						String name = attr.getKey();
						if (name.startsWith("[")) {
							attributeBinding(name, attr.getValue(), classNames, attrNames, excludeBindings);
						} else if (!excludeBindings.contains(name)) {
							boolean hasValue = attr.getValue() != null;
							handler.attributeStart(name, hasValue);
							if (hasValue) {
								replaceExpr(attr.getValue());
								handler.attributeEnd();
							}
						}
					});
		}

		if (cmpClass != null) {
			addHostAttributeBindings(cmpClass, classNames, attrNames, excludeBindings);
		}

		if (!classNames.isEmpty()) {
			handler.attributeClasses(classNames);
		}

		if (!attrNames.isEmpty()) {
			attrNames.forEach(it -> handler.attributeExpr(it[0], it[1]));
		}
	}

	private void addHostAttributeBindings(Class<?> cmpClass, List<String[]> classNames, List<String[]> attrNames, Set<String> excludeBindings) {
		for (Field f : cmpClass.getFields()) {
			HostBinding hb = f.getAnnotation(HostBinding.class);
			if (hb == null) {
				continue;
			}

			attributeBinding(format("[%s]", hb.value()), f.getName(), classNames, attrNames, excludeBindings);
		}

		for (Method m : cmpClass.getMethods()) {
			HostBinding hb = m.getAnnotation(HostBinding.class);
			if (hb == null) {
				continue;
			}

			if (m.getParameterCount() > 0) {
				throw new ParseException("host binding method must not have parameters: %s.%s", cmpClass.getName(), m.getName());
			}

			attributeBinding(format("[%s]", hb.value()), format("%s()", m.getName()), classNames, attrNames, excludeBindings);
		}
	}

	private void attributeBinding(String name, String value, List<String[]> classNames, List<String[]> attrNames, Set<String> exclude) {
		if (!name.endsWith("]")) {
			throw new ParseException("attribute binding malformed: missing ]");
		}
		String rawName = name.substring(1, name.length() - 1);

		if (exclude.contains(rawName)) {
			return;
		}

		if (rawName.equals("class")) {
			ObjParser.parse(value)
					.forEach((key, expr) -> classNames.add(new String[] { key, expr }));
		} else if (rawName.startsWith(BINDING_CLASS)) {
			String className = rawName.substring(BINDING_CLASS.length());
			classNames.add(new String[] { className, value });
		} else if (rawName.startsWith(BINDING_ATTR)) {
			String attrName = rawName.substring(BINDING_ATTR.length());
			attrNames.add(new String[] { attrName, value });
		} else {
			handler.attributeExpr(rawName, value);
		}
	}

	private void replaceExpr(@Nullable String text) {
		if (text == null || text.isEmpty()) {
			return;
		}
		Matcher matcher = EXPR_PATTERN.matcher(text);
		int last = 0;
		while (matcher.find()) {
			String expr = matcher.group(1);
			String left = text.substring(last, matcher.start());
			handler.text(left, false, null);
			handleExpr(expr);
			last = matcher.end();
		}

		if (last < text.length()) {
			handler.text(text.substring(last), false, null);
		}
	}

	private void handleExpr(String expr) {
		Matcher matcher = PIPE_PATTERN.matcher(expr);
		matcher.find();
		String exprHead = matcher.group(1)
				.trim();
		List<String[]> pipes = new ArrayList<>();
		while (matcher.find()) {
			String pipe = matcher.group(1)
					.trim();

			Matcher paramMatcher = PIPE_PARAM_PATTERN.matcher(pipe);
			String param;
			if (paramMatcher.find()) {
				pipe = paramMatcher.group(1);
				param = paramMatcher.group(2);
			} else {
				param = "";
			}

			Class<?> resolvedPipe = resolver.resolvePipe(pipe);
			if (resolvedPipe == null) {
				throw new ParseException("pipe not found for name '%s'", pipe);
			}

			pipes.add(param.isEmpty() ? new String[] { resolvedPipe.getName() } : new String[] { resolvedPipe.getName(), param });
		}
		handler.text(exprHead, true, pipes);
	}

	private void elementConditionalElse(Element el) {
		String ngIf = el.attr("*ngIf");
		int pos = ngIf.lastIndexOf(";");
		if (pos < 0) {
			return;
		}

		String right = ngIf.substring(pos + 1)
				.trim();
		if (right.startsWith("else")) {
			String templateRef = right.substring("else".length())
					.trim();
			Element template = findTemplate(templateRef, el.ownerDocument());
			handler.elementConditionalElse();
			accept(template.childNodes());
		}
	}

	private void compileDirectives(List<CmpRef> cmpRefs, Element el) {
		for (CmpRef cmpRef : cmpRefs) {
			Object dir = resolver.getInjector()
					.get(cmpRef.clazz);

			if (dir instanceof OnCompile) {
				String cmpClass = resolver.resolveCmpClass(handler.cmpClassesStack.peek());
				((OnCompile) dir).ngOnCompile(new JSoupElementRef(el), cmpClass);
			}
		}

	}

	private boolean inlineComponent(Element el) {
		return inlineComponents || el.is(ContainerComponent.SELECTOR);
	}

	private void endElement(Element el) {
		if (!el.tag()
				.isSelfClosing()) {

			if (inlineComponent(el)) {
				if (!cmpElements.remove(el)) {
					handler.elementEnd(el.nodeName());
				}
			} else {
				handler.elementEnd(el.nodeName());
			}
		}

		if (el.equals(elementConditionals.peek())) {
			elementConditionals.pop();
			elementConditionalElse(el);
			handler.elementConditionalEnd();
		}

		if (el.equals(elementRepeated.peek())) {
			elementRepeated.pop();
			handler.elementRepeatedEnd();
		}
	}
}
