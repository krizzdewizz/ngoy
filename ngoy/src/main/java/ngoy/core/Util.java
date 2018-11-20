package ngoy.core;

import static ngoy.core.NgoyException.wrap;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;

import org.apache.commons.text.StringEscapeUtils;

/**
 * Utils.
 * 
 * @author krizz
 */
public class Util {

	private Util() {
	}

	/**
	 * Returns a new UTF-8 encoded print stream that prints to the given output
	 * stream.
	 * 
	 * @param out
	 * @return PrintStream
	 */
	public static PrintStream newPrintStream(OutputStream out) {
		try {
			return new PrintStream(out, true, "UTF-8");
		} catch (Exception e) {
			throw wrap(e);
		}
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

	/**
	 * HTML escape's the given text.
	 * 
	 * @param text
	 * @return Escaped text
	 */
	public static String escapeHtml(String text) {
		return StringEscapeUtils.escapeHtml4(text);
	}

	/**
	 * XML escape's the given text.
	 * 
	 * @param text
	 * @return Escaped text
	 */
	public static String escapeXml(String text) {
		return StringEscapeUtils.escapeXml11(text);
	}

	/**
	 * Escapes based on the given contentType.
	 * <p>
	 * <ul>
	 * <li><code>"text/xml"</code>: {@link #escapeXml(String)}</li>
	 * <li><code>"text/plain"</code>: don't escape</li>
	 * <li>All others: {@link #escapeHtml(String)}</li>
	 * </ul>
	 * 
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
