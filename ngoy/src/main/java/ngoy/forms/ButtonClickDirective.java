package ngoy.forms;

import static ngoy.core.dom.XDom.appendChild;
import static ngoy.core.dom.XDom.cloneNode;
import static ngoy.core.dom.XDom.removeContents;
import static ngoy.core.dom.XDom.setNodeName;

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
	public void onCompile(Jerry el, String componentClass) {
		Attribute clickAttr = XDom.getAttributes(el)
				.stream()
				.filter(a -> a.getName()
						.contains("click"))
				.findFirst()
				.get();

		String controllerAttr = clickAttr.getName()
				.replace("(click)", "controller");
		String controllerMethod = clickAttr.getValue();

		Jerry form = XDom.createElement("form", el);
		Jerry buttonClone = appendChild(form, cloneNode(el));
		buttonClone.removeAttr(clickAttr.getName());
		buttonClone.attr("type", "submit");

		for (Attribute attr : XDom.getAttributes(el)) {
			el.removeAttr(attr.getName());
		}
		removeContents(el);
		setNodeName(el, "ng-template");

		form.attr("method", "POST");
		form.attr(controllerAttr, controllerMethod);

		appendChild(el, form);
	}

}
