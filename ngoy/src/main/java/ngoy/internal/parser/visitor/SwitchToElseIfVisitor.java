package ngoy.internal.parser.visitor;

import static java.lang.String.format;
import static jodd.lagarto.dom.Node.NodeType.ELEMENT;
import static ngoy.core.dom.XDom.appendChild;
import static ngoy.core.dom.XDom.cloneNode;
import static ngoy.core.dom.XDom.createElement;
import static ngoy.core.dom.XDom.getNodeName;
import static ngoy.internal.parser.Parser.NG_TEMPLATE;

import jodd.jerry.Jerry;
import jodd.lagarto.dom.Node;
import ngoy.core.dom.NodeVisitor;
import ngoy.core.dom.XDom;
import ngoy.core.internal.ContainerComponent;

public class SwitchToElseIfVisitor implements NodeVisitor {

	private final NodeVisitor target;
	private int nextRefIndex;

	public SwitchToElseIfVisitor(NodeVisitor target) {
		this.target = target;
	}

	@Override
	public void start(Jerry el) {
		if (el.get(0)
				.getNodeType() == ELEMENT) {

			String ngSwitch = el.attr("[ngSwitch]");
			if (ngSwitch == null) {
				target.start(el);
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

			Node tpll = tpl.get(0);
			if (ContainerComponent.SELECTOR.equals(getNodeName(elClone))) {
				elClone.contents()
						.each((c, index) -> {
							tpll.insertChild(c.get(0), index);
							return null;
						});
			} else {
				tpll.insertChild(elClone.get(0), 0);
			}
		}

		target.start(el);
	}

	private String nextRef() {
		return format("case%s", nextRefIndex++);
	}

	@Override
	public void end(Jerry node) {
		target.end(node);
	}

}
