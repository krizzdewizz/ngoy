package ngoy.internal.parser.visitor;

import static java.lang.String.format;
import static ngoy.core.dom.XDom.appendChild;
import static ngoy.core.dom.XDom.cloneNode;
import static ngoy.core.dom.XDom.createElement;
import static ngoy.internal.parser.Parser.NG_TEMPLATE;

import jodd.jerry.Jerry;
import jodd.lagarto.dom.Element;
import ngoy.core.dom.NodeVisitor;
import ngoy.core.dom.XDom;

public class SwitchToElseIfVisitor implements NodeVisitor {

	private final NodeVisitor src;
	private int nextRefIndex;

	public SwitchToElseIfVisitor(NodeVisitor src) {
		this.src = src;
	}

	@Override
	public void head(Jerry el) {
		if (el.get(0) instanceof Element) {

			String ngSwitch = el.attr("[ngSwitch]");
			if (ngSwitch == null) {
				src.head(el);
				return;
			}

			el.removeAttr("[ngSwitch]");

			Jerry elClone = cloneNode(el);

			XDom.removeContents(el);
			Jerry tpl = appendChild(el, createElement(NG_TEMPLATE, el));

			tpl.attr("ngIfForSwitch", null);
			tpl.attr("[ngIf]", ngSwitch);

			int i = 0;
			for (Jerry casee : elClone.$("[\\[ngSwitchCase\\]]")) {
				String caseExpr = casee.attr("[ngSwitchCase]");
				String ref = nextRef();
				String tag = i == 0 ? "ngElseIfFirst" : "ngElseIf";
				tpl.attr(format("%s-%s", tag, ref), caseExpr);
				casee.attr(format("#%s", ref), null);
				casee.removeAttr("[ngSwitchCase]");
				i++;
			}

			Jerry def = elClone.$("[ngSwitchDefault]")
					.first();
			if (def.length() > 0) {
				String ref = nextRef();
				tpl.attr("ngElse", ref);
				def.attr(format("#%s", ref), null);
				def.removeAttr("ngSwitchDefault");
			}

			tpl.get(0)
					.insertChild(elClone.get(0), 0);
		}

		src.head(el);
	}

	private String nextRef() {
		return format("case%s", nextRefIndex++);
	}

	@Override
	public void tail(Jerry node) {
		src.tail(node);
	}

}
