package org.ngoy.internal.parser.visitor;

import static java.lang.String.format;

import java.util.ArrayList;

import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.select.NodeVisitor;

public class SwitchToElseIfVisitor implements NodeVisitor {

	private final NodeVisitor src;
	private int nextRefIndex;

	public SwitchToElseIfVisitor(NodeVisitor src) {
		this.src = src;
	}

	@Override
	public void head(Node node, int depth) {
		if (node instanceof Element) {

			Element el = (Element) node;
			String ngSwitch = el.attr("[ngSwitch]");
			if (ngSwitch.isEmpty()) {
				src.head(node, depth);
				return;
			}

			el.removeAttr("[ngSwitch]");

			Element elClone = el.clone();

			Element tpl = el.ownerDocument()
					.createElement("ng-template");
			new ArrayList<>(el.childNodes()).forEach(Node::remove);
			el.appendChild(tpl);

			tpl.attr("ngIfForSwitch", null);
			tpl.attr("[ngIf]", ngSwitch);

			int i = 0;
			for (Element casee : elClone.select("[[ngSwitchCase]]")) {
				String caseExpr = casee.attr("[ngSwitchCase]");
				String ref = nextRef();
				String tag = i == 0 ? "ngElseIfFirst" : "ngElseIf";
				tpl.attr(format("%s-%s", tag, ref), caseExpr);
				casee.attr(format("#%s", ref), null);
				casee.removeAttr("[ngSwitchCase]");
				i++;
			}

			Element def = elClone.select("[ngSwitchDefault]")
					.first();
			if (def != null) {
				String ref = nextRef();
				tpl.attr("ngElse", ref);
				def.attr(format("#%s", ref), null);
				def.removeAttr("ngSwitchDefault");
			}

			tpl.insertChildren(0, elClone.childNodes());
		}

		src.head(node, depth);
	}

	private String nextRef() {
		return format("case%s", nextRefIndex++);
	}

	@Override
	public void tail(Node node, int depth) {
		src.tail(node, depth);
	}

}
