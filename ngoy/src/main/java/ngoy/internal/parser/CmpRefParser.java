package ngoy.internal.parser;

import static java.util.Collections.emptyList;
import static java.util.Collections.emptySet;
import static java.util.stream.Collectors.toList;
import static ngoy.core.dom.XDom.accept;
import static ngoy.core.dom.XDom.getClassList;
import static ngoy.core.dom.XDom.getNodeName;
import static ngoy.core.dom.XDom.getStyleList;
import static ngoy.internal.parser.Inputs.cmpInputs;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import jodd.jerry.Jerry;
import jodd.lagarto.dom.Node;
import ngoy.core.Nullable;
import ngoy.core.internal.CmpRef;

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

	void acceptCmpRefs(Jerry el, List<CmpRef> cmpRefs) {

		List<String[]> classNames = getClassList(el).stream()
				.map(it -> new String[] { it, "" })
				.collect(toList());

		List<String[]> styleNames = getStyleList(el).stream()
				.map(it -> new String[] { it, "" })
				.collect(toList());

		List<String[]> attrNames = new ArrayList<>();

		if (cmpRefs.isEmpty()) {
			if (parser.inlineComponent(el)) {
				parser.cmpElements.add(el);
			} else {
				parser.handler.elementHead(getNodeName(el));
				AttributeBinding.replaceAttrs(parser, el, emptySet(), classNames, attrNames, styleNames);
				AttributeBinding.replaceAttrExpr(parser, classNames, attrNames, styleNames);
				parser.handler.elementHeadEnd();
			}
			return;
		}

		List<CmpRef> allDirs = new ArrayList<>();
		List<CmpRef> allCmps = new ArrayList<>();
		splitComponentsAndDirectives(cmpRefs, allCmps, allDirs);

		Set<String> excludeBindings = new HashSet<>();
		List<String> cmpInputs = allCmps.isEmpty() ? emptyList() : cmpInputs(el, allCmps.get(0).clazz, excludeBindings, parser.resolver);
		List<List<String>> dirInputs = allDirs.stream()
				.map(ref -> cmpInputs(el, ref.clazz, excludeBindings, parser.resolver))
				.collect(toList());

		boolean hadElementHead = false;

		if (!allDirs.isEmpty()) {
			parser.handler.elementHead(getNodeName(el));
			AttributeBinding.replaceAttrs(parser, el, excludeBindings, classNames, attrNames, styleNames);
			AttributeBinding.replaceAttrExpr(parser, classNames, attrNames, styleNames);
			hadElementHead = true;

			int i = 0;
			for (CmpRef ref : allDirs) {
				parser.handler.componentStart(ref, dirInputs.get(i));

				List<String[]> cNames = new ArrayList<>();
				List<String[]> aNames = new ArrayList<>();
				List<String[]> sNames = new ArrayList<>();
				AttributeBinding.addHostAttributeBindings(parser, ref.clazz, excludeBindings, cNames, aNames, sNames);
				AttributeBinding.replaceAttrExpr(parser, cNames, aNames, sNames);
				parser.handler.componentEnd();
				i++;
			}

			if (allCmps.isEmpty()) {
				parser.handler.elementHeadEnd();
			}
		}

		if (!allCmps.isEmpty()) {
			CmpRef ref = allCmps.get(0);
			parser.handler.componentStart(ref, cmpInputs);

			if (parser.inlineComponent(el)) {
				parser.cmpElements.add(el);
			} else {
				if (!hadElementHead) {
					parser.handler.elementHead(getNodeName(el));
					AttributeBinding.replaceAttrs(parser, el, excludeBindings, classNames, attrNames, styleNames);
					AttributeBinding.replaceAttrExpr(parser, classNames, attrNames, styleNames);
				}

				List<String[]> cNames = new ArrayList<>();
				List<String[]> aNames = new ArrayList<>();
				List<String[]> sNames = new ArrayList<>();
				AttributeBinding.addHostAttributeBindings(parser, ref.clazz, excludeBindings, cNames, aNames, sNames);
				AttributeBinding.replaceAttrExpr(parser, cNames, aNames, sNames);
				parser.handler.elementHeadEnd();
			}

			acceptCmpRef(el, ref);

			parser.handler.componentEnd();

			parser.skipSubTreeVisitor.skipSubTree(el);
		}
	}

	private void acceptCmpRef(Jerry el, CmpRef ref) {
		Jerry cmpNodes = parser.parse(ref.template);

		Jerry ngContentEl = findNgContent(cmpNodes);

		if (ngContentEl.length() == 0) {
			parser.accept(cmpNodes);
			return;
		}

		Node ngContentEll = ngContentEl.get(0);
		boolean invokeHandler = !ngContentEll.hasAttribute("scope");
		String select = ngContentEl.attr("select");
		String selector = select == null ? ngContentEl.attr("selector") : select;

		Jerry parent = ngContentEl.parent();

		Jerry childNodes = parent.contents();
		int ngContentIndex = childNodes.index(ngContentEll);

		List<Jerry> nodesBefore = new ArrayList<>();
		List<Jerry> nodesAfter = new ArrayList<>();

		childNodes.each((n, i) -> {
			if (i < ngContentIndex) {
				nodesBefore.add(n);
			} else if (i > ngContentIndex) {
				nodesAfter.add(n);
			}
			return true;
		});

		parent.get(0)
				.removeChild(ngContentEll);

		parser.accept(nodesBefore);

		if (invokeHandler) {
			parser.handler.ngContentStart();
		}

		if (selector == null) {
			parser.accept(el.contents());
		} else {
			accept(el.$(selector), parser.visitor);
		}

		if (invokeHandler) {
			parser.handler.ngContentEnd();
		}

		parser.accept(nodesAfter);
	}

	@Nullable
	private Jerry findNgContent(Jerry cmpNodes) {
		return cmpNodes.$("ng-content");
	}
}
