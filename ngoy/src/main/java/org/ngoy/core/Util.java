package org.ngoy.core;

import static org.ngoy.core.NgoyException.wrap;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
import java.util.stream.Stream;

import org.apache.commons.text.StringEscapeUtils;

public class Util {
	private static final String SET_PREFIX = "set";

	private Util() {
	}

	public static PrintStream newPrintStream(OutputStream out) {
		try {
			return new PrintStream(out, true, "UTF-8");
		} catch (Exception e) {
			throw wrap(e);
		}
	}

	public static void copy(InputStream in, OutputStream out) {
		byte[] buffer = new byte[1024];
		int len;
		try {
			while ((len = in.read(buffer)) > -1) {
				out.write(buffer, 0, len);
			}
		} catch (Exception e) {
			throw wrap(e);
		}
	}

	public static String copyToString(InputStream in) {
		try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
			copy(in, out);
			return new String(out.toByteArray(), "UTF-8");
		} catch (Exception e) {
			throw wrap(e);
		}
	}

	@Nullable
	public static Annotation findAnnotation(AnnotatedElement el, String name) {
		return Stream.of(el.getAnnotations())
				.filter(it -> name.equals(it.annotationType()
						.getSimpleName()))
				.findFirst()
				.orElse(null);
	}

	public static boolean isSet(@Nullable String s) {
		return s != null && !s.isEmpty();
	}

	public static String escapeHtml(String text) {
		return StringEscapeUtils.escapeHtml4(text);
	}

	public static String escapeXml(String text) {
		return StringEscapeUtils.escapeXml11(text);
	}

	/**
	 * @param contentType null or empty to use default
	 */
	public static String escape(String text, @Nullable String contentType) {
		if ("text/xml".equals(contentType)) {
			return escapeXml(text);
		}
		if ("text/plain".equals(contentType)) {
			return text;
		}
		return escapeHtml(text);
	}

	public static String escapeJava(String text) {
		return StringEscapeUtils.escapeJava(text);
	}

	public static String fieldName(String setter) {
		if (setter.startsWith(SET_PREFIX)) {
			String right = setter.substring(SET_PREFIX.length());
			if (right.isEmpty()) {
				return SET_PREFIX;
			}
			return Character.toLowerCase(right.charAt(0)) + right.substring(1);
		}
		return setter;
	}

	public static Method findSetter(Class<?> clazz, String name) {
		return Stream.of(clazz.getMethods())
				.filter(m -> m.getName()
						.equals(name) && m.getParameterCount() == 1)
				.findFirst()
				.orElseThrow(() -> new NgoyException("Setter method %s%s could not be found or has not exactly 1 parameter", clazz.getName(), name));
	}

	/**
	 * non-api
	 */
	public static String getTemplate(Class<?> clazz) {
		Component cmp = clazz.getAnnotation(Component.class);
		if (cmp == null) {
			throw new NgoyException("Annotation %s not found on class %s", Component.class.getName(), clazz.getName());
		}
		String templateUrl = cmp.templateUrl();
		String tpl;
		if (isSet(templateUrl)) {
			InputStream in = clazz.getResourceAsStream(templateUrl);
			if (in == null) {
				throw new NgoyException("Template could not be found: '%s'", templateUrl);
			}
			try (InputStream inn = in) {
				tpl = copyToString(inn);
			} catch (Exception e) {
				throw wrap(e);
			}
		} else {
			tpl = cmp.template();
		}
		return tpl;

	}
}
