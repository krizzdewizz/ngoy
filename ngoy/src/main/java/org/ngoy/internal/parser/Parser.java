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
import org.jsoup.nodes.Attribute;
import org.jsoup.nodes.CDataNode;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.NodeVisitor;
import org.ngoy.core.NgoyException;
import org.ngoy.core.Nullable;
import org.ngoy.core.OnCompile;
import org.ngoy.core.PipeTransform;
import org.ngoy.core.internal.CmpRef;
import org.ngoy.core.internal.ContainerComponent;
import org.ngoy.core.internal.Resolver;
import org.ngoy.internal.parser.visitor.DefaultVisitor;
import org.ngoy.internal.parser.visitor.MicroSyntaxVisitor;
import org.ngoy.internal.parser.visitor.SkipSubTreeVisitor;
import org.ngoy.internal.parser.visitor.SwitchToElseIfVisitor;

public class Parser {

	static Document parseHtml(String template) {
		try {
			return Jsoup.parse(template, "");
		} catch (Exception e) {
			throw wrap(e);
		}
	}

	public List<Node> parse(String template, boolean forceNoText) {
		try {
			boolean text = "text/plain".equals(contentType);
			if (text && !forceNoText) {
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

	public static final String NG_TEMPLATE = "ng-template";
	private static final String NG_ELSE = "ngElse";

	private static final Pattern EXPR_PATTERN = Pattern.compile("\\{\\{(.*?)\\}\\}", Pattern.MULTILINE);
	private static final Pattern PIPE_PATTERN = Pattern.compile("([^\\|]+)");
	private static final Pattern NG_CONTAINER_PATTERN = Pattern.compile("<ng-container(.*)>((.|\\s)*)</ng-container>", Pattern.MULTILINE);
	private static final Pattern NG_FOR_PATTERN = Pattern.compile("\\*ngFor=\"(.*)\"");

	private final LinkedList<Element> elementConditionals = new LinkedList<>();
	private final LinkedList<Element> elementRepeated = new LinkedList<>();
	public boolean inlineComponents;
	public String contentType;

	final Set<Element> cmpElements = new HashSet<>();
	MyHandler handler;
	private Resolver resolver;
	private final CmpRefParser cmpRefParser;

	NodeVisitor visitor;
	SkipSubTreeVisitor skipSubTreeVisitor;

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
	 * @param resolver if null, uses {@link Resolver#DEFAULT}
	 */
	public Parser(@Nullable Resolver resolver) {
		this.cmpRefParser = new CmpRefParser(this);
		this.resolver = resolver == null ? Resolver.DEFAULT : resolver;
	}

	public void parse(String template, ParserHandler handler, Boolean parseBody) {

		Objects.requireNonNull(template, "template must not be null.");

		this.handler = new MyHandler(handler);
		skipSubTreeVisitor = new SkipSubTreeVisitor(new Visitor());
		visitor = new MicroSyntaxVisitor(new SwitchToElseIfVisitor(skipSubTreeVisitor));

		boolean body = parseBody == null ? "text/plain".equals(contentType) : parseBody.booleanValue();

		List<Node> nodes = body ? parse(template, false) : asList(parseHtml(template));

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

		text = ForOfMicroParser.parse(text);

		Element el;
		String content;
		Matcher matcher = NG_CONTAINER_PATTERN.matcher(text);
		if (matcher.find()) {
			el = node.ownerDocument()
					.createElement(NG_TEMPLATE);
			content = matcher.group(2);
			Matcher forMatcher = NG_FOR_PATTERN.matcher(matcher.group(1));
			if (forMatcher.find()) {
				el.attr("*ngFor", forMatcher.group(1));
			}

			el.traverse(new MicroSyntaxVisitor(new DefaultVisitor()));
		} else {
			content = text;
			el = null;
		}

		boolean cmpEnd = el != null && replaceElement(el, false);
		replaceExpr(content);

		if (el != null) {
			if (cmpEnd) {
				handler.componentEnd();
				handler.ngContentEnd();
			}
			visitor.tail(el, -1);
		}
	}

	private boolean replaceElement(Node node, boolean acceptChildren) {
		Element el = (Element) node;

		if (el.nodeName()
				.equals(NG_TEMPLATE)) {
			String ngIf = el.attr("[ngIf]");
			if (!ngIf.isEmpty()) {
				replaceNgIf(el, ngIf);
			} else if (ForOfVariable.parse(el, handler::elementRepeatedStart)) {
				elementRepeated.push(el);
			} else {
				skipSubTreeVisitor.skipSubTree(el);
			}

			return false;
		}

		List<CmpRef> cmpRefs = resolver.resolveCmps(el);
		compileCmps(cmpRefs, el);

		boolean hasCmps = cmpRefParser.acceptCmpRefs(el, cmpRefs, acceptChildren);
		if (!hasCmps) {
			acceptSpecialElementsContent(el);
		}

		if (isSet(handler.textOverrideExpr)) {
			skipSubTreeVisitor.skipSubTree(el);
			handler.textOverrideExpr = null;
		}

		return hasCmps;
	}

	private void replaceNgIf(Element el, String ngIf) {
		elementConditionals.push(el);

		boolean isSwitch = el.hasAttr("ngIfForSwitch");
		String firstCaseTpl = "";
		String switchExpr = "";
		if (isSwitch) {
			for (Attribute a : el.attributes()) {
				if (a.getKey()
						.startsWith("ngElseIfFirst-")) {
					firstCaseTpl = a.getKey()
							.substring("ngElseIfFirst-".length());
					switchExpr = a.getValue();
					break;
				}
			}

			if (switchExpr.isEmpty()) {
				throw new NgoyException("ngSwitch must have at least one ngSwitchCase: %s", el.cssSelector());
			}
		}
		handler.elementConditionalStart(ngIf, switchExpr);
		if (!switchExpr.isEmpty()) {
			skipSubTreeVisitor.skipSubTree(null);
			accept(findTemplate(firstCaseTpl, el.ownerDocument()).childNodes());
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
		Element template = document.selectFirst(format("%s[#%s]", NG_TEMPLATE, ref));
		if (template == null) {
			throw new ParseException("No <%s> found for name %s", NG_TEMPLATE, ref);
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
		List<List<String>> pipes = new ArrayList<>();
		while (matcher.find()) {
			String pipe = matcher.group(1)
					.trim();

			List<String> pipeAndParams = new ArrayList<>();
			pipe = PipeParser.parsePipe(pipe, pipeAndParams);

			Class<?> resolvedPipe = resolver.resolvePipe(pipe);
			if (resolvedPipe == null) {
				throw new ParseException("Pipe not found for name '%s'", pipe);
			} else if (!PipeTransform.class.isAssignableFrom(resolvedPipe)) {
				throw new ParseException("Pipe %s must implement %s", resolvedPipe.getName(), PipeTransform.class.getName());
			}

			pipeAndParams.add(0, resolvedPipe.getName());
			pipes.add(pipeAndParams);
		}
		handler.text(exprHead, true, pipes);
	}

	private void elementConditionalElse(Element el) {
		String ngElse = el.attr(NG_ELSE);
		if (ngElse.isEmpty()) {
			return;
		}
		handler.elementConditionalElse();
		skipSubTreeVisitor.skipSubTree(null);
		accept(findTemplate(ngElse, el.ownerDocument()).childNodes());
	}

	private void elementConditionalElseIf(Element el) {
		for (Attribute attr : el.attributes()) {
			String name = attr.getKey();
			if (!name.startsWith("ngElseIf-")) {
				continue;
			}

			String tpl = name.substring(name.indexOf('-') + 1);
			String expr = attr.getValue();

			handler.elementConditionalElseIf(expr);
			skipSubTreeVisitor.skipSubTree(null);
			accept(findTemplate(tpl, el.ownerDocument()).childNodes());
		}
	}

	private void compileCmps(List<CmpRef> cmpRefs, Element el) {
		for (CmpRef cmpRef : cmpRefs) {
			Object cmp = resolver.getInjector()
					.get(cmpRef.clazz);

			if (cmp instanceof OnCompile) {
				String cmpClass = resolver.resolveCmpClass(handler.cmpClassesStack.peek());
				((OnCompile) cmp).ngOnCompile(el, cmpClass);
			}
		}
	}

	boolean inlineComponent(Element el) {
		return inlineComponents || el.is(ContainerComponent.SELECTOR);
	}

	private void endElement(Element el) {
		if (!el.tag()
				.isSelfClosing()
				&& !el.nodeName()
						.equals(NG_TEMPLATE)) {

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
			elementConditionalElseIf(el);
			elementConditionalElse(el);
			handler.elementConditionalEnd();
		}

		if (el.equals(elementRepeated.peek())) {
			elementRepeated.pop();
			handler.elementRepeatedEnd();
		}
	}
}
