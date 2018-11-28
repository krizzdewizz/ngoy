package ngoy.forms;

import static ngoy.core.dom.NgoyElement.setNodeName;
import static ngoy.core.dom.XDom.appendChild;
import static ngoy.core.dom.XDom.cloneNode;
import static ngoy.core.dom.XDom.removeContents;

import jodd.jerry.Jerry;
import jodd.lagarto.dom.Attribute;
import ngoy.core.Directive;
import ngoy.core.OnCompile;
import ngoy.core.dom.XDom;

/**
 * experimental.
 * 
 * @author krizz
 */
@Directive(selector = "button[\\(click\\)], button[\\[\\(click\\)\\]]")
public class ButtonClickDirective implements OnCompile {

	@Override
	public void ngOnCompile(Jerry el, String componentClass) {
		Attribute clickAttr = XDom.getAttributes(el)
				.stream()
				.filter(a -> a.getName()
						.contains("click"))
				.findFirst()
				.get();

		String actionAttr = clickAttr.getName()
				.replace("(click)", "action");
		String action = clickAttr.getValue();

		Jerry buttonClone = cloneNode(el);
		buttonClone.removeAttr(clickAttr.getName());
		buttonClone.attr("type", "submit");

		setNodeName(el, "form");
		for (Attribute attr : XDom.getAttributes(el)) {
			el.removeAttr(attr.getName());
		}
		removeContents(el);
		el.attr("method", "POST");
		el.attr(actionAttr, action);
		appendChild(el, buttonClone);
	}

}
