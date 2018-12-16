package ngoy.forms;

import static java.lang.String.format;
import static ngoy.core.NgoyException.wrap;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import ngoy.core.AppRoot;
import ngoy.core.Component;
import ngoy.core.HostBinding;
import ngoy.core.Inject;
import ngoy.core.Input;
import ngoy.core.NgoyException;
import ngoy.core.OnInit;
import ngoy.core.Util;

@Component(selector = "form", template = "<ng-content></ng-content><input *ngFor=\"let input of formInputs\" type=\"hidden\" [name]=\"input.getKey()\" [value]=\"input.getValue()\">")
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

		formAction = findControllerMethodAction(appRoot.getAppRootClass()
				.getName(), controllerMethod);
	}

	public static String findControllerMethodAction(String componentClass, String controllerMethod) {
		try {
			Class<?> clazz = Thread.currentThread()
					.getContextClassLoader()
					.loadClass(componentClass);

			String requestPath = findRequestMapping(clazz).map(FormComponent::findPath)
					.orElse("");

			String postPath = Stream.of(clazz.getMethods())
					.filter(method -> method.getName()
							.equals(controllerMethod))
					.findFirst()
					.flatMap(FormComponent::findPostMapping)
					.map(FormComponent::findPath)
					.orElseThrow(() -> new NgoyException("Controller method %s.%s with a @PostMapping annotation could not be not found", componentClass, controllerMethod));

			String path = requestPath.isEmpty() ? postPath : format("%s/%s", requestPath, postPath);
			return path.replace("*", "")
					.replace("//", "/");
		} catch (Exception e) {
			throw wrap(e);
		}
	}

	private static Optional<Annotation> findRequestMapping(Class<?> clazz) {
		return Stream.of(clazz.getAnnotations())
				.filter(ann -> ann.annotationType()
						.getSimpleName()
						.equals("RequestMapping"))
				.findFirst();
	}

	private static Optional<Annotation> findPostMapping(Method method) {
		return Stream.of(method.getAnnotations())
				.filter(ann -> ann.annotationType()
						.getSimpleName()
						.equals("PostMapping"))
				.findFirst();
	}

	private static String findPath(Annotation ann) {
		Class<?> at = ann.annotationType();
		String path = "";
		try {
			Object value;
			value = at.getMethod("name")
					.invoke(ann);
			if (value instanceof String) {
				path = (String) value;
			}
			if (path.isEmpty()) {
				value = at.getMethod("value")
						.invoke(ann);
				if (value instanceof String[]) {
					String[] sa = (String[]) value;
					path = Stream.of(sa)
							.filter(Util::isSet)
							.findFirst()
							.orElse("");
				}
			}
		} catch (Exception e) {
			// ignore
		}
		return path;
	}
}
