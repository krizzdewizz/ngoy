package ngoy.internal.parser;

import static java.util.Collections.emptyList;
import static java.util.Collections.emptySet;
import static java.util.stream.Collectors.toList;
import static ngoy.core.dom.XDom.accept;
import static ngoy.core.dom.XDom.getClassList;
import static ngoy.core.dom.XDom.getNodeName;
import static ngoy.core.dom.XDom.getStyleList;
import static ngoy.core.dom.XDom.isEqualNode;
import static ngoy.internal.parser.AttributeBinding.addHostAttributeBindings;
import static ngoy.internal.parser.AttributeBinding.replaceAttrs;
import static ngoy.internal.parser.Inputs.cmpInputs;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import jodd.jerry.Jerry;
import ngoy.core.dom.NodeVisitor;
import ngoy.core.internal.CmpRef;
import ngoy.core.internal.Scope;
import ngoy.internal.parser.Inputs.CmpInput;

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
	private final Set<Class<?>> acceptedCmpRefs = new HashSet<>();

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
			if (parser.inlineAll) {
				parser.cmpElements.add(el);
			} else {
				parser.handler.elementHead(getNodeName(el));
				replaceAttrs(parser, el, emptySet(), classNames, attrNames, styleNames);
				parser.handler.elementHeadEnd();
			}
			return;
		}

		List<CmpRef> allDirs = new ArrayList<>();
		List<CmpRef> allCmps = new ArrayList<>();
		splitComponentsAndDirectives(cmpRefs, allCmps, allDirs);

		Set<String> excludeBindings = new HashSet<>();
		List<CmpInput> cmpInputs = allCmps.isEmpty() ? emptyList() : cmpInputs(el, allCmps.get(0).clazz, excludeBindings);
		List<List<CmpInput>> dirInputs = allDirs.stream()
				.map(ref -> cmpInputs(el, ref.clazz, excludeBindings))
				.collect(toList());

		boolean hadElementHead = false;

		if (!allDirs.isEmpty()) {
			parser.handler.elementHead(getNodeName(el));
			replaceAttrs(parser, el, excludeBindings, classNames, attrNames, styleNames);
			hadElementHead = true;

			int i = 0;
			for (CmpRef ref : allDirs) {
				parser.handler.componentStartInput(ref, false, dirInputs.get(i));
				parser.handler.componentStart(ref);

				List<String[]> cNames = new ArrayList<>();
				List<String[]> aNames = new ArrayList<>();
				List<String[]> sNames = new ArrayList<>();
				addHostAttributeBindings(parser, ref.clazz, excludeBindings, cNames, aNames, sNames);
				parser.handler.componentEnd();
				i++;
			}

			if (allCmps.isEmpty()) {
				parser.handler.elementHeadEnd();
			}
		}

		if (!allCmps.isEmpty()) {
			CmpRef ref = allCmps.get(0);
			parser.handler.componentStartInput(ref, false, cmpInputs);

			if (parser.inlineComponent(el)) {
				parser.handler.componentStart(ref);
				parser.handler.componentContentStart(ref);
				parser.cmpElements.add(el);
			} else {
				if (!hadElementHead) {
					parser.handler.elementHead(getNodeName(el));
					replaceAttrs(parser, el, excludeBindings, classNames, attrNames, styleNames);
				}

				parser.handler.componentStart(ref);

				List<String[]> cNames = new ArrayList<>();
				List<String[]> aNames = new ArrayList<>();
				List<String[]> sNames = new ArrayList<>();
				addHostAttributeBindings(parser, ref.clazz, excludeBindings, cNames, aNames, sNames);
				parser.handler.elementHeadEnd();
				parser.handler.componentContentStart(ref);
			}

			acceptCmpRef(el, ref);

			parser.handler.componentEnd();

			parser.skipSubTreeVisitor.skipSubTree(el);
		}
	}

	private void acceptCmpRef(Jerry el, CmpRef ref) {
		if (acceptedCmpRefs.contains(ref.clazz)) {
			return;
		}

		acceptedCmpRefs.add(ref.clazz);

		Jerry cmpTpl = parser.parse(ref.template);

		Jerry ngContentEl = cmpTpl.$("ng-content");

		if (ngContentEl.length() == 0) {
			parser.accept(cmpTpl);
			return;
		}

		boolean invokeHandler = ref.clazz.getAnnotation(Scope.class) == null;
		String select = ngContentEl.attr("select");
		String selector = select == null ? ngContentEl.attr("selector") : select;
		Jerry elContents = selector == null ? el.contents() : el.$(selector);

		accept(cmpTpl, new NodeVisitor() {
			@Override
			public void start(Jerry node) {
				if (isEqualNode(node, ngContentEl)) {
					if (invokeHandler) {
						parser.handler.ngContentStart();
					}

					parser.accept(elContents);

					if (invokeHandler) {
						parser.handler.ngContentEnd();
					}
				} else {
					parser.visitor.start(node);
				}
			}

			@Override
			public void end(Jerry node) {
				if (!isEqualNode(node, ngContentEl)) {
					parser.visitor.end(node);
				}
			}
		});
	}
}
