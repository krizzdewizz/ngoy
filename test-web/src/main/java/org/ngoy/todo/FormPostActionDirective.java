package org.ngoy.todo;

import static org.ngoy.core.NgoyException.wrap;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Optional;
import java.util.stream.Stream;

import org.jsoup.nodes.Element;
import org.ngoy.core.Directive;
import org.ngoy.core.ElementRef;
import org.ngoy.core.NgoyException;
import org.ngoy.core.OnCompile;
import org.ngoy.core.internal.JSoupElementRef;
import org.ngoy.internal.util.Util;

@Directive(selector = "[ngoyFormPost]")
public class FormPostActionDirective implements OnCompile {

	@Override
	public void ngOnCompile(ElementRef elRef, String cmpClass) {
		try {
			Element el = ((JSoupElementRef) elRef).getNativeElement();
			String controllerMethod = el.attr("ngoyFormPost");

			if (controllerMethod.isEmpty()) {
				throw new NgoyException("'ngoyFormPost' attribute must not be empty on '%s'", el.cssSelector());
			}

			Class<?> clazz = Thread.currentThread()
					.getContextClassLoader()
					.loadClass(cmpClass);

			String path = Stream.of(clazz.getMethods())
					.filter(method -> method.getName()
							.equals(controllerMethod))
					.findFirst()
					.flatMap(this::findPostMapping)
					.map(this::findPath)
					.orElseThrow(() -> new NgoyException("Controller method %s.%s with a @PostMapping annotation could not be not found", cmpClass, controllerMethod));

			el.attr("action", path);
			el.attr("method", "post");

		} catch (Exception e) {
			throw wrap(e);
		}
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
