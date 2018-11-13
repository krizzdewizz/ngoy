package org.ngoy.internal.parser;

import static java.lang.String.format;
import static java.util.Arrays.asList;
import static org.ngoy.core.NgoyException.wrap;
import static org.ngoy.core.Util.isSet;

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

	List<Node> parseBody(String template, boolean forceHtmlContentType) {
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

	private static final Pattern PIPE_PARAM_PATTERN = Pattern.compile("(.*?):(.*)");
	private static final Pattern EXPR_PATTERN = Pattern.compile("\\{\\{(.*?)\\}\\}", Pattern.MULTILINE);
	private static final Pattern PIPE_PATTERN = Pattern.compile("([^\\|]+)");
	private static final Pattern NG_CONTAINER_PATTERN = Pattern.compile("<ng-container(.*)>((.|\\s)*)</ng-container>", Pattern.MULTILINE);
	private static final Pattern NG_FOR_PATTERN = Pattern.compile("\\*ngFor=\"(.*)\"");

	private final LinkedList<Element> elementConditionals = new LinkedList<>();
	private final LinkedList<Element> elementRepeated = new LinkedList<>();
	public boolean parseBody;
	public boolean inlineComponents;
	public String contentType;

	final Set<Element> cmpElements = new HashSet<>();
	MyHandler handler;
	SkipSubTreeVisitor visitor;
	private Resolver resolver;
	private final CmpRefParser cmpRefParser;

	static class MyHandler extends ParserHandler.Delegate {
		final LinkedList<String> cmpClassesStack = new LinkedList<>();
		private String textOverrideExpr;

		public MyHandler(ParserHandler target) {
			super(target);
		}

		@Override
		public void componentStart(String clazz, List<String> params) {
			super.componentStart(clazz, params);
			cmpClassesStack.push(clazz);
		}

		@Override
		public void elementHead(String name) {
			super.elementHead(name);
		}

		@Override
		public void componentEnd() {
			super.componentEnd();
			cmpClassesStack.pop();
		}

		@Override
		public void textOverride(String expr) {
			super.textOverride(expr);
			textOverrideExpr = expr;
		}
	}

	public Parser() {
		this(null);
	}

	/**
	 * @param resolver
	 *            if null, uses {@link Resolver#DEFAULT}
	 */
	public Parser(@Nullable Resolver resolver) {
		this.cmpRefParser = new CmpRefParser(this);
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

		Element el = node.ownerDocument()
				.createElement("ng-container");
		String content;
		Matcher matcher = NG_CONTAINER_PATTERN.matcher(text);
		if (matcher.find()) {
			content = matcher.group(2);
			Matcher forMatcher = NG_FOR_PATTERN.matcher(matcher.group(1));
			if (forMatcher.find()) {
				el.attr("*ngFor", forMatcher.group(1));
			}
		} else {
			content = text;
		}

		replaceElement(el, false);
		replaceExpr(content);

		handler.componentEnd();
		handler.ngContentEnd();
		visitor.tail(el, -1);
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
			handler.elementRepeatedStart(ForOfVariable.parseNgFor(ngFor), ForOfVariable.parse(ngFor));
		}

		List<CmpRef> cmpRefs = resolver.resolveCmps(new JSoupElementRef(el));
		compileCmps(cmpRefs, el);

		if (!cmpRefParser.acceptCmpRefs(el, cmpRefs, acceptChildren)) {
			acceptSpecialElementsContent(el);
		}

		if (isSet(handler.textOverrideExpr)) {
			visitor.skipSubTree(el);
			handler.textOverrideExpr = null;
		}
	}

	void accept(List<Node> nodes) {
		nodes.forEach(n -> n.traverse(visitor));
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
			throw new ParseException("No <ng-template> found for name %s", ref);
		}

		return template;
	}

	void replaceExpr(@Nullable String text) {
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
				throw new ParseException("Pipe not found for name '%s'", pipe);
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

	private void compileCmps(List<CmpRef> cmpRefs, Element el) {
		for (CmpRef cmpRef : cmpRefs) {
			Object cmp = resolver.getInjector()
					.get(cmpRef.clazz);

			if (cmp instanceof OnCompile) {
				String cmpClass = resolver.resolveCmpClass(handler.cmpClassesStack.peek());
				((OnCompile) cmp).ngOnCompile(new JSoupElementRef(el), cmpClass);
			}
		}
	}

	boolean inlineComponent(Element el) {
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
