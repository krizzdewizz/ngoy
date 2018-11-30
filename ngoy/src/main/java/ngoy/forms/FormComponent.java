package ngoy.forms;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ngoy.core.AppRoot;
import ngoy.core.Component;
import ngoy.core.HostBinding;
import ngoy.core.Inject;
import ngoy.core.Input;
import ngoy.core.NgoyException;
import ngoy.core.OnInit;

@Component(selector = "form", template = "<ng-content></ng-content><input *ngFor=\"let input of formInputs\" type=\"hidden\" [name]=\"input.key\" [value]=\"input.value\">")
public class FormComponent implements OnInit {

	@Input
	public Object controller;

	@HostBinding("attr.action")
	public String formAction;

	public Set<Map.Entry<String, Object>> formInputs = new HashSet<>();

	@Inject
	public AppRoot appRoot;

	@Override
	public void ngOnInit() {
		formInputs.clear();
		formAction = null;

		if (controller == null) {
			return;
		}

		String controllerMethod;
		if (controller instanceof List) {
			List<?> list = (List<?>) controller;
			if (list.isEmpty()) {
				throw new NgoyException("FormComponent: action input expects controller method name as first parameter.");
			}
			controllerMethod = String.valueOf(list.get(0));
			if (list.size() > 1) {
				@SuppressWarnings("unchecked")
				Map<String, Object> map = (Map<String, Object>) list.get(1);
				formInputs.addAll(map.entrySet());
			}
		} else {
			controllerMethod = String.valueOf(controller);
		}

		formAction = FormPostActionDirective.findControllerMethodAction(appRoot.getAppRootClass()
				.getName(), controllerMethod)
				.replace("*", "")
				.replace("//", "/");
	}
}
