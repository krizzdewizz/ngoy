package ngoy.internal.parser;

import static java.lang.String.format;
import static ngoy.core.NgoyException.wrap;
import static ngoy.core.Util.isSet;
import static ngoy.internal.parser.NgoyElement.getPosition;
import static ngoy.internal.parser.visitor.XDom.attributes;
import static ngoy.internal.parser.visitor.XDom.nodeName;
import static ngoy.internal.parser.visitor.XDom.traverse;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import jodd.jerry.Jerry;
import jodd.lagarto.dom.Attribute;
import jodd.lagarto.dom.Element;
import jodd.lagarto.dom.Node;
import jodd.lagarto.dom.Node.NodeType;
import jodd.lagarto.dom.Text;
import ngoy.core.Component;
import ngoy.core.NgoyException;
import ngoy.core.Nullable;
import ngoy.core.OnCompile;
import ngoy.core.internal.CmpRef;
import ngoy.core.internal.ContainerComponent;
import ngoy.core.internal.Resolver;
import ngoy.internal.parser.visitor.MicroSyntaxVisitor;
import ngoy.internal.parser.visitor.NodeVisitor;
import ngoy.internal.parser.visitor.SkipSubTreeVisitor;
import ngoy.internal.parser.visitor.SwitchToElseIfVisitor;

public class Parser {

	public static Jerry parseHtml(String template) {
		try {
			Jerry doc = Jerry.jerry(new NgoyDomBuilder())
					.parse(template);
			return doc;
		} catch (Exception e) {
			throw wrap(e);
		}
	}

	public Jerry parse(String template) {
		try {
			Jerry doc = parseHtml(template);

			if ("text/plain".equals(contentType)) {
				doc = parseHtml(ForOfMicroParser.parse(doc.get(0)
						.getHtml()));
			}

			return doc;
		} catch (Exception e) {
			throw wrap(e);
		}
	}

	private class Visitor implements NodeVisitor {

		Jerry currentEl;

		@Override
		public void head(Jerry node, int depth) {

			replaceCommentLikeNodes(node);

			Node n = node.get(0);
			if (n instanceof Text) {
				replaceExpr(((Text) n).getTextContent());
			} else if (n instanceof Element) {
				currentEl = node;
				replaceElement(node);
			}
		}

		@Override
		public void tail(Jerry node, int depth) {
			if (node.get(0) instanceof Element) {
				endElement(node);
			}
			currentEl = null;
		}
	}

	public static final String NG_TEMPLATE = "ng-template";
	private static final String NG_ELSE = "ngElse";

	private final LinkedList<Jerry> elementConditionals = new LinkedList<>();
	private final LinkedList<Jerry> elementRepeated = new LinkedList<>();
	public boolean inlineComponents;
	public String contentType;

	final Set<Jerry> cmpElements = new HashSet<>();
	MyHandler handler;
	private Resolver resolver;
	private final CmpRefParser cmpRefParser;

	NodeVisitor visitor;
	SkipSubTreeVisitor skipSubTreeVisitor;
	Visitor replacingVisitor;

	static class MyHandler extends ParserHandler.Delegate {
		final LinkedList<CmpRef> cmpClassesStack = new LinkedList<>();
		private String textOverrideExpr;

		public MyHandler(ParserHandler target) {
			super(target);
		}

		@Override
		public void componentStart(CmpRef cmpRef, List<String> params) {
			super.componentStart(cmpRef, params);
			cmpClassesStack.push(cmpRef);
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

	public void parse(String template, ParserHandler handler) {

		Objects.requireNonNull(template, "template must not be null.");

		this.handler = new MyHandler(handler);
		replacingVisitor = new Visitor();
		skipSubTreeVisitor = new SkipSubTreeVisitor(replacingVisitor);
		visitor = new MicroSyntaxVisitor(new SwitchToElseIfVisitor(skipSubTreeVisitor));

		Jerry nodes = parse(template);

		try {
			acceptDocument(nodes);
		} catch (Exception e) {
			String message = e.getMessage();
			throw new NgoyException(e, "Error while parsing: %s%s", isSet(message) ? message : e, exceptionInfo());
		}
	}

	private void acceptDocument(Jerry nodes) {
		this.handler.documentStart();
		accept(nodes);
		this.handler.documentEnd();
	}

	private boolean replaceElement(Jerry el) {

		if (nodeName(el).equals(NG_TEMPLATE)) {
			String ngIf = el.attr("[ngIf]");
			if (ngIf != null) {
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

		boolean hasCmps = cmpRefParser.acceptCmpRefs(el, cmpRefs);

		if (isSet(handler.textOverrideExpr)) {
			skipSubTreeVisitor.skipSubTree(el);
			handler.textOverrideExpr = null;
		}

		return hasCmps;
	}

	private void replaceNgIf(Jerry el, String ngIf) {
		elementConditionals.push(el);

		Node ell = el.get(0);
		boolean isSwitch = ell.hasAttribute("ngIfForSwitch");
		String firstCaseTpl = "";
		String switchExpr = "";
		if (isSwitch) {
			for (Attribute attr : attributes(el)) {
				if (attr.getName()
						.startsWith("ngElseIfFirst-")) {
					firstCaseTpl = attr.getName()
							.substring("ngElseIfFirst-".length());
					switchExpr = attr.getValue();
					break;
				}
			}

			if (switchExpr.isEmpty()) {
				throw new NgoyException("ngSwitch must have at least one ngSwitchCase: %s", ell.getCssPath());
			}
		}
		handler.elementConditionalStart(ngIf, switchExpr);
		if (!switchExpr.isEmpty()) {
			acceptTemplate(firstCaseTpl, el.root());
		}
	}

	public void accept(List<Jerry> nodes) {
		nodes.forEach(this::accept);
	}

	public void accept(Jerry nodes) {
		traverse(nodes, visitor);
	}

	private void acceptTemplate(String ref, Jerry document) {
		Jerry template = document.$(format("%s[\\#%s]", NG_TEMPLATE, ref))
				.first();
		if (template.length() == 0) {
			throw new ParseException("No <%s> found for name %s", NG_TEMPLATE, ref);
		}

		skipSubTreeVisitor.skipSubTree(null);
		accept(template.contents());
	}

	void replaceExpr(@Nullable String text) {
		if (text == null || text.isEmpty()) {
			return;
		}

		ExprParser.parse(text, resolver, (s, isExpr) -> handler.text(s, isExpr, true));
	}

	private void elementConditionalElse(Jerry el) {
		String ngElse = el.attr(NG_ELSE);
		if (ngElse == null) {
			return;
		}
		handler.elementConditionalElse();
		acceptTemplate(ngElse, el.root());
	}

	private void elementConditionalElseIf(Jerry el) {
		for (Attribute attr : attributes(el)) {
			String name = attr.getName();
			if (!name.startsWith("ngElseIf-")) {
				continue;
			}

			String tpl = name.substring(name.indexOf('-') + 1);
			String expr = attr.getValue();

			handler.elementConditionalElseIf(expr);
			acceptTemplate(tpl, el.root());
		}
	}

	private void compileCmps(List<CmpRef> cmpRefs, Jerry el) {
		for (CmpRef cmpRef : cmpRefs) {
			Object cmp = resolver.getInjector()
					.get(cmpRef.clazz);

			if (cmp instanceof OnCompile) {
				((OnCompile) cmp).ngOnCompile(el, topCmpClass().getName());
			}
		}
	}

	boolean inlineComponent(Jerry el) {
		return inlineComponents || el.is(ContainerComponent.SELECTOR);
	}

	private void endElement(Jerry el) {
		Element ell = (Element) el.get(0);
		String nodeName = ell.getNodeName();
		if (!ell.isVoidElement() && !nodeName.equals(NG_TEMPLATE)) {

			if (inlineComponent(el)) {
				if (!cmpElements.remove(el)) {
					handler.elementEnd(nodeName);
				}
			} else {
				handler.elementEnd(nodeName);
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

	Class<?> topCmpClass() {
		CmpRef peek = handler.cmpClassesStack.peek();
		return resolver.resolveCmpClass(peek == null ? null : peek.clazz);
	}

	String exceptionInfo() {
		CmpRef peek = handler.cmpClassesStack.peek();
		Class<?> topCmpClass = null;
		String templateUrl = "";
		String className = "";
		Jerry currentEl = replacingVisitor.currentEl;
		String position = currentEl == null ? "" : getPosition(currentEl);
		if (peek != null) {
			topCmpClass = topCmpClass();
			if (topCmpClass != null) {
				className = topCmpClass.getName();
				Component cmp = topCmpClass.getAnnotation(Component.class);
				if (cmp != null) {
					templateUrl = cmp.templateUrl();
				}
			}
		}

		return format("\nComponent: %s\ntemplateUrl: %s\nposition: %s", className, templateUrl, position);
	}

	private void replaceCommentLikeNodes(Jerry node) {
		Node n = node.get(0);
		NodeType nodeType = n.getNodeType();
		if (nodeType == NodeType.DOCUMENT_TYPE || nodeType == NodeType.COMMENT || nodeType == NodeType.CDATA || nodeType == NodeType.XML_DECLARATION) {
			handler.text(n.getHtml(), false, false);
		}
	}
}
