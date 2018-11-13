package org.ngoy.internal.parser;

import static java.util.Collections.emptyList;
import static java.util.Collections.emptySet;
import static java.util.stream.Collectors.toList;
import static org.ngoy.internal.parser.Inputs.cmpInputs;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.ngoy.core.Nullable;
import org.ngoy.core.internal.CmpRef;

public class CmpRefParser {

	private static void splitComponentsAndDirectives(List<CmpRef> cmpRefs, List<CmpRef> cmps, List<CmpRef> dirs) {
		for (CmpRef ref : cmpRefs) {
			if (ref.directive) {
				dirs.add(ref);
			} else {
				cmps.add(ref);
			}
		}
	}

	private final Parser parser;

	public CmpRefParser(Parser parser) {
		this.parser = parser;
	}

	boolean acceptCmpRefs(Element el, List<CmpRef> cmpRefs, boolean acceptChildren) {

		List<String[]> classNames = el.classNames()
				.stream()
				.map(it -> new String[] { it, "" })
				.collect(toList());
		List<String[]> attrNames = new ArrayList<>();

		if (cmpRefs.isEmpty()) {
			if (parser.inlineComponent(el)) {
				parser.cmpElements.add(el);
			} else {
				parser.handler.elementHead(el.nodeName());
				AttributeBinding.replaceAttrs(parser, el, emptySet(), classNames, attrNames);
				AttributeBinding.replaceAttrExpr(parser, classNames, attrNames);
				parser.handler.elementHeadEnd();
			}
			return false;
		}

		List<CmpRef> allDirs = new ArrayList<>();
		List<CmpRef> allCmps = new ArrayList<>();
		splitComponentsAndDirectives(cmpRefs, allCmps, allDirs);

		Set<String> excludeBindings = new HashSet<>();
		List<String> cmpInputs = allCmps.isEmpty() ? emptyList() : cmpInputs(el, allCmps.get(0).clazz, excludeBindings);
		List<List<String>> dirInputs = allDirs.stream()
				.map(ref -> cmpInputs(el, ref.clazz, excludeBindings))
				.collect(toList());

		boolean hadElementHead = false;

		if (!allDirs.isEmpty()) {
			parser.handler.elementHead(el.nodeName());
			AttributeBinding.replaceAttrs(parser, el, excludeBindings, classNames, attrNames);
			AttributeBinding.replaceAttrExpr(parser, classNames, attrNames);
			hadElementHead = true;

			int i = 0;
			for (CmpRef ref : allDirs) {
				parser.handler.componentStart(ref.clazz.getName(), dirInputs.get(i));

				List<String[]> cNames = new ArrayList<>();
				List<String[]> aNames = new ArrayList<>();
				AttributeBinding.addHostAttributeBindings(parser, ref.clazz, excludeBindings, cNames, aNames);
				AttributeBinding.replaceAttrExpr(parser, cNames, aNames);
				parser.handler.componentEnd();
				i++;
			}

			if (allCmps.isEmpty()) {
				parser.handler.elementHeadEnd();
			}
		}

		if (!allCmps.isEmpty()) {
			CmpRef ref = allCmps.get(0);
			parser.handler.componentStart(ref.clazz.getName(), cmpInputs);

			if (parser.inlineComponent(el)) {
				parser.cmpElements.add(el);
			} else {
				if (!hadElementHead) {
					parser.handler.elementHead(el.nodeName());
					AttributeBinding.replaceAttrs(parser, el, excludeBindings, classNames, attrNames);
					AttributeBinding.replaceAttrExpr(parser, classNames, attrNames);
				}

				List<String[]> cNames = new ArrayList<>();
				List<String[]> aNames = new ArrayList<>();
				AttributeBinding.addHostAttributeBindings(parser, ref.clazz, excludeBindings, cNames, aNames);
				AttributeBinding.replaceAttrExpr(parser, cNames, aNames);
				parser.handler.elementHeadEnd();
			}

			acceptCmpRef(el, ref, acceptChildren);

			if (acceptChildren) {
				parser.handler.componentEnd();
			}

			parser.visitor.skipSubTree(el);
		}

		return true;
	}

	private void acceptCmpRef(Element el, CmpRef ref, boolean acceptChildren) {
		List<Node> cmpNodes = parser.parseBody(ref.template, true);

		Element ngContentEl = findNgContent(cmpNodes);

		if (ngContentEl == null) {
			parser.accept(cmpNodes);
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
			parser.accept(nodesBefore);
		}

		if (invokeHandler) {
			parser.handler.ngContentStart();
		}

		if (acceptChildren) {
			if (selector.isEmpty()) {
				parser.accept(el.childNodes());
			} else {
				el.childNodes()
						.stream()
						.filter(Element.class::isInstance)
						.map(Element.class::cast)
						.filter(e -> e.is(selector))
						.forEach(e -> e.traverse(parser.visitor));
			}

			if (invokeHandler) {
				parser.handler.ngContentEnd();
			}

			parser.accept(nodesAfter);
		}
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

}
