package ngoy.core;

import static java.lang.String.format;
import static ngoy.core.NgoyException.wrap;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.LineNumberReader;
import java.io.OutputStream;
import java.io.StringReader;
import java.lang.reflect.Method;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.codehaus.commons.compiler.CompileException;

/**
 * Utils.
 * 
 * @author krizz
 */
public class Util {

	private Util() {
	}

	/**
	 * Copies all bytes from in o out.
	 * 
	 * @param in
	 * @param out
	 */
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

	/**
	 * Copies all bytes to a string using UTF-8 encoding.
	 * 
	 * @param in
	 * @return String
	 */
	public static String copyToString(InputStream in) {
		try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
			copy(in, out);
			return new String(out.toByteArray(), "UTF-8");
		} catch (Exception e) {
			throw wrap(e);
		}
	}

	/**
	 * @return true if the given string is not null and not empty
	 */
	public static boolean isSet(@Nullable String s) {
		return s != null && !s.isEmpty();
	}

	public static String escapeJava(String text) {
		StringBuilder sb = null;
		for (int i = 0, n = text.length(); i < n; i++) {
			String repl = null;
			char c = text.charAt(i);
			switch (c) {
			case '"':
				repl = "\\\"";
				break;
			case '\\':
				repl = "\\\\";
				break;
			case '\t':
				repl = "\\t";
				break;
			case '\n':
				repl = "\\n";
				break;
			case '\r':
				repl = "\\r";
				break;
			case '\b':
				repl = "\\b";
				break;
			case '\f':
				repl = "\\f";
				break;
			default:
				if (sb != null) {
					sb.append(c);
				}
			}

			if (repl != null) {
				if (sb == null) {
					sb = new StringBuilder(text.substring(0, i));
				}
				sb.append(repl);
			}
		}
		return sb != null ? sb.toString() : text;
	}

	/**
	 * HTML/XML escape's the given text.
	 * 
	 * @param text
	 * @return Escaped text
	 */
	public static String escapeHtmlXml(String text) {
		StringBuilder sb = null;
		for (int i = 0, n = text.length(); i < n; i++) {
			String repl = null;
			char c = text.charAt(i);
			switch (c) {
			case '"':
				repl = "&quot;";
				break;
			case '&':
				repl = "&amp;";
				break;
			case '<':
				repl = "&lt;";
				break;
			case '>':
				repl = "&gt;";
				break;
			default:
				if (sb != null) {
					sb.append(c);
				}
			}

			if (repl != null) {
				if (sb == null) {
					sb = new StringBuilder(text.substring(0, i));
				}
				sb.append(repl);
			}
		}
		return sb != null ? sb.toString() : text;
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

	/**
	 * Returns the line at the given line number.
	 * 
	 * @param s          string with lines
	 * @param lineNumber starting at 1
	 * @return Line
	 */
	public static String getLine(String s, int lineNumber) {
		try {
			LineNumberReader lnr = new LineNumberReader(new StringReader(s));
			int i = 1;
			String line;
			while ((line = lnr.readLine()) != null) {
				if (i == lineNumber) {
					return line;
				}
				i++;
			}
			return format("<line %s not found>", lineNumber);
		} catch (Exception e) {
			throw wrap(e);
		}
	}

	public static String getCompileExceptionMessageWithoutLocation(CompileException compileException) {
		String message = compileException.getMessage();
		if (compileException.getLocation() != null) {
			Matcher matcher = Pattern.compile(".*Column \\d*:(.*)")
					.matcher(message);
			if (matcher.find()) {
				return matcher.group(1)
						.trim();
			}
		}

		return message;
	}

	public static Method toGetter(String name, Function<String, Method> getters) {
		String right = name.substring(0, 1)
				.toUpperCase() + name.substring(1);
		Method meth;
		if ((meth = getters.apply("get" + right)) != null) {
			return meth;
		}
		if ((meth = getters.apply("is" + right)) != null) {
			return meth;
		}

		return null;
	}

	public static String primitiveToRefType(Class<?> clazz) {
		boolean primitive = isPrimitive(clazz);
		if (!primitive) {
			return clazz.getName();
		}

		if (clazz == char.class) {
			return Character.class.getSimpleName();
		} else if (clazz == int.class) {
			return Integer.class.getSimpleName();
		}

		String clazzName = clazz.getName();
		return clazzName.substring(0, 1)
				.toUpperCase() + clazzName.substring(1);
	}

	public static boolean isPrimitive(Class<?> clazz) {
		return clazz == boolean.class //
				|| clazz == byte.class //
				|| clazz == char.class //
				|| clazz == short.class //
				|| clazz == int.class //
				|| clazz == long.class //
				|| clazz == float.class //
				|| clazz == double.class;
	}
}
