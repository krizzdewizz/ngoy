package ngoy.internal.parser;

import static java.util.Collections.emptyList;
import static java.util.Collections.emptySet;
import static java.util.stream.Collectors.toList;
import static ngoy.internal.parser.Inputs.cmpInputs;
import static ngoy.internal.parser.visitor.XDom.classNames;
import static ngoy.internal.parser.visitor.XDom.nodeName;
import static ngoy.internal.parser.visitor.XDom.styleNames;
import static ngoy.internal.parser.visitor.XDom.traverse;

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

	boolean acceptCmpRefs(Jerry el, List<CmpRef> cmpRefs, boolean acceptChildren) {

		List<String[]> classNames = classNames(el).stream()
				.map(it -> new String[] { it, "" })
				.collect(toList());

		List<String[]> styleNames = styleNames(el).stream()
				.map(it -> new String[] { it, "" })
				.collect(toList());

		List<String[]> attrNames = new ArrayList<>();

		if (cmpRefs.isEmpty()) {
			if (parser.inlineComponent(el)) {
				parser.cmpElements.add(el);
			} else {
				parser.handler.elementHead(nodeName(el));
				AttributeBinding.replaceAttrs(parser, el, emptySet(), classNames, attrNames, styleNames);
				AttributeBinding.replaceAttrExpr(parser, classNames, attrNames, styleNames);
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
			parser.handler.elementHead(nodeName(el));
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
					parser.handler.elementHead(nodeName(el));
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

			acceptCmpRef(el, ref, acceptChildren);

			if (acceptChildren) {
				parser.handler.componentEnd();
			}

			parser.skipSubTreeVisitor.skipSubTree(el);
		}

		return true;
	}

	private void acceptCmpRef(Jerry el, CmpRef ref, boolean acceptChildren) {
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

		Jerry childNodes = parent.contents();// new ArrayList<>(asList(parent.getChildNodes()));
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

		if (acceptChildren) {
			parser.accept(nodesBefore);
		}

		if (invokeHandler) {
			parser.handler.ngContentStart();
		}

		if (acceptChildren) {
			if (selector == null) {
				parser.accept(el.contents());
			} else {
				traverse(el.$(selector), parser.visitor);
			}

			if (invokeHandler) {
				parser.handler.ngContentEnd();
			}

			parser.accept(nodesAfter);
		}
	}

	@Nullable
	private Jerry findNgContent(Jerry cmpNodes) {
		return cmpNodes.$("ng-content");
	}
}