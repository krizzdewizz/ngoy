package ngoy.forms;

import static java.lang.String.format;
import static ngoy.core.NgoyException.wrap;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Optional;
import java.util.stream.Stream;

import jodd.jerry.Jerry;
import ngoy.core.Directive;
import ngoy.core.NgoyException;
import ngoy.core.OnCompile;
import ngoy.core.Util;

/**
 * experimental.
 * 
 * @author krizz
 */
@Directive(selector = "[ngoyFormPost]")
public class FormPostActionDirective implements OnCompile {

	@Override
	public void ngOnCompile(Jerry el, String componentClass) {
		String controllerMethod = el.attr("ngoyFormPost");

		if (controllerMethod.isEmpty()) {
			throw new NgoyException("'ngoyFormPost' attribute must not be empty on '%s'", el.get(0)
					.getCssPath());
		}

		String path = findControllerMethodAction(componentClass, controllerMethod);

		el.attr("action", path);
		el.attr("method", "post");
	}

	public static String findControllerMethodAction(String componentClass, String controllerMethod) {
		try {
			Class<?> clazz = Thread.currentThread()
					.getContextClassLoader()
					.loadClass(componentClass);

			String requestPath = findRequestMapping(clazz).map(FormPostActionDirective::findPath)
					.orElse("");

			String postPath = Stream.of(clazz.getMethods())
					.filter(method -> method.getName()
							.equals(controllerMethod))
					.findFirst()
					.flatMap(FormPostActionDirective::findPostMapping)
					.map(FormPostActionDirective::findPath)
					.orElseThrow(() -> new NgoyException("Controller method %s.%s with a @PostMapping annotation could not be not found", componentClass, controllerMethod));

			String path = requestPath.isEmpty() ? postPath : format("%s/%s", requestPath, postPath);
			return path;
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
