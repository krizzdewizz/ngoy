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

@Directive(selector = "[ngoyFormPost]")
public class FormPostActionDirective implements OnCompile {

	@Override
	public void ngOnCompile(Jerry el, String cmpClass) {
		try {
			String controllerMethod = el.attr("ngoyFormPost");

			if (controllerMethod.isEmpty()) {
				throw new NgoyException("'ngoyFormPost' attribute must not be empty on '%s'", el.get(0)
						.getCssPath());
			}

			Class<?> clazz = Thread.currentThread()
					.getContextClassLoader()
					.loadClass(cmpClass);

			String requestPath = findRequestMapping(clazz).map(this::findPath)
					.orElse("");

			String postPath = Stream.of(clazz.getMethods())
					.filter(method -> method.getName()
							.equals(controllerMethod))
					.findFirst()
					.flatMap(this::findPostMapping)
					.map(this::findPath)
					.orElseThrow(() -> new NgoyException("Controller method %s.%s with a @PostMapping annotation could not be not found", cmpClass, controllerMethod));

			String path = requestPath.isEmpty() ? postPath : format("%s/%s", requestPath, postPath);

			el.attr("action", path);
			el.attr("method", "post");

		} catch (Exception e) {
			throw wrap(e);
		}
	}

	private Optional<Annotation> findRequestMapping(Class<?> clazz) {
		return Stream.of(clazz.getAnnotations())
				.filter(ann -> ann.annotationType()
						.getSimpleName()
						.equals("RequestMapping"))
				.findFirst();
	}

	private Optional<Annotation> findPostMapping(Method method) {
		return Stream.of(method.getAnnotations())
				.filter(ann -> ann.annotationType()
						.getSimpleName()
						.equals("PostMapping"))
				.findFirst();
	}

	private String findPath(Annotation ann) {
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
