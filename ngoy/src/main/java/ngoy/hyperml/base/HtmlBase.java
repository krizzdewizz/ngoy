package ngoy.hyperml.base;

import static java.util.Arrays.asList;
import static ngoy.core.FlatList.flatten;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import ngoy.core.NgoyException;

/**
 * Clients may subclass to add custom behaviour.
 * 
 * @author krizz
 * @param <T> type of subclass
 */
public abstract class HtmlBase<T extends HtmlBase<?>> extends HtmlCore<T> {

	private static final Set<String> VOID_ELEMENTS = new HashSet<>(
			Arrays.asList("area", "base", "br", "col", "embed", "hr", "img", "input", "keygen", "link", "meta", "param", "source", "track", "wbr"));

	@Override
	protected boolean isVoidElement(String name) {
		return VOID_ELEMENTS.contains(name.toLowerCase());
	}

	@Override
	protected boolean escapeText() {
		if (inCss) {
			return false;
		}
		Object name = stack.peek();
		if (name == null) {
			return true;
		}
		String lower = name.toString()
				.toLowerCase();
		return !lower.equals("script") && !lower.equals("style");
	}

	private boolean inCss;

	/**
	 * Outputs a CSS style declaration as a {@link #text(Object...)} call.
	 * 
	 * @param selector        The selector
	 * @param styleValuePairs css style/value pairs such as
	 *                        <code>["color", "red", "display", "none"]</code>
	 */
	public T css(String selector, Object... styleValuePairs) {
		if (inCss) {
			throw new NgoyException("Nested css() calls are not allowed");
		}
		Object[] pairs = checkPairs(mergeUnits(flatten(styleValuePairs)));
		if (pairs.length == 0) {
			inCss = true;
			return text(selector, "{");
		}

		text(selector, "{");
		cssBlockStyles(pairs);
		text("}");

		return _this();
	}

	private T cssBlockStyles(Object[] pairs) {
		for (int i = 0, n = pairs.length; i < n; i += 2) {
			text(pairs[i], ":", pairs[i + 1], ";");
		}
		return _this();
	}

	@Override
	public T $() {
		if (inCss) {
			inCss = false;
			return text("}");
		}
		return super.$();
	}

	@Override
	public T $(Object name, Object... params) {
		if (inCss) {
			List<Object> pairs = new ArrayList<>();
			pairs.add(name);
			pairs.addAll(asList(flatten(params)));
			return cssBlockStyles(checkPairs(mergeUnits(pairs.toArray())));
		}
		return super.$(name, params);
	}

	private Object[] mergeUnits(Object[] pairs) {
		if (pairs.length == 0) {
			return pairs;
		}
		List<Object> result = new ArrayList<>();
		for (int i = 0, n = pairs.length; i < n; i += 2) {
			result.add(pairs[i]);
			if (i + 1 >= n) {
				throw new NgoyException("Pairs array length must be even");
			}
			Object second = pairs[i + 1];
			int next = i + 2;
			if (next < n) {
				Object nextObj = pairs[next];
				if (nextObj instanceof Unit) {
					second = second.toString() + nextObj;
					i += 1;
				}
			}
			result.add(second);
		}
		return result.toArray();
	}

	private Object[] checkPairs(Object[] pairs) {
		if (pairs.length % 2 != 0) {
			throw new NgoyException("Pairs array length must be even");
		}
		return pairs;
	}

	/**
	 * Returns a space delimited class list for the given map.
	 * <p>
	 * Entry key is the class name, which is added to the list if the entry value
	 * evaluates to true.
	 * <p>
	 * Example:
	 * 
	 * <pre>
	 * Map&lt;String, Boolean&gt; classes = new HashMap&lt;&gt;();
	* classes.put(&quot;peter&quot;, true);
	* classes.put(&quot;paul&quot;, false);
	* classes.put(&quot;mary&quot;, Boolean.TRUE);
	*
	* a(classs, classes(classes), $);	
	* 
	 * &lt;a class="peter mary"&gt;&lt;/a&gt
	 * </pre>
	 * 
	 * @param classBooleanPairs [String, Boolean] pairs
	 * @return class list
	 */
	public String classes(Map<String, Boolean> classBooleanPairs) {
		StringBuilder sb = new StringBuilder();
		for (Entry<String, Boolean> entry : classBooleanPairs.entrySet()) {
			if (entry.getValue()) {
				if (sb.length() > 0) {
					sb.append(' ');
				}
				sb.append(entry.getKey());
			}
		}
		return sb.toString();
	}

	/**
	 * Returns a space delimited class list for the given pairs.
	 * <p>
	 * First element is the class name (Object), which is added to the list if the
	 * second element (Boolean) evaluates to true.
	 * <p>
	 * Example:
	 * 
	 * <pre>
	 * a(classs, classes("peter", true, "paul", false, "mary", Boolean.TRUE), $);
	 * 
	 * &lt;a class="peter mary"&gt;&lt;/a&gt
	 * </pre>
	 * 
	 * @param clazz     first class
	 * @param include   whether to include clazz in the list
	 * @param morePairs [Object, Boolean] pairs
	 * @return class list
	 */
	public String classes(Object clazz, boolean include, Object... morePairs) {
		Object[] pairs = checkPairs(flatten(morePairs));
		StringBuilder sb = new StringBuilder();
		if (include) {
			sb.append(clazz);
		}
		for (int i = 0, n = pairs.length; i < n; i += 2) {
			Boolean pred = (Boolean) pairs[i + 1];
			if (pred) {
				if (sb.length() > 0) {
					sb.append(' ');
				}
				sb.append(pairs[i]);
			}
		}
		return sb.toString();
	}

	/**
	 * Returns a semicolon delimited style list for the given map.
	 * <p>
	 * Entry key is the style name, which is added to the list if the entry value
	 * evaluates to a non-null, non-empty string.
	 * <p>
	 * Example:
	 * 
	 * <pre>
	 * Map&lt;String, String&gt; styles = new HashMap&lt;&gt;();
	 * styles.put(&quot;background-color&quot;, &quot;red&quot;);
	 * styles.put(&quot;color&quot;, null);
	 * styles.put(&quot;display&quot;, &quot;none&quot;);
	 *
	 * a(style, styles(styles), $);
	 * 
	 * &lt;a style="background-color:red;display:none"&gt;&lt;/a&gt
	 * </pre>
	 * 
	 * @param styleValuePairs [String, Object] pairs
	 * @return style list
	 */
	public String styles(Map<String, ?> styleValuePairs) {
		StringBuilder sb = new StringBuilder();
		for (Entry<String, ?> entry : styleValuePairs.entrySet()) {
			Object value = entry.getValue();
			if (value != null && !value.toString()
					.isEmpty()) {
				if (sb.length() > 0) {
					sb.append(';');
				}
				appendStyle(entry.getKey(), value, sb);
			}
		}
		return sb.toString();
	}

	/**
	 * Returns a semicolon delimited style list for the given pairs.
	 * <p>
	 * First element is the style name (Object), which is added to the list if the
	 * second element, the style's value (Object) evaluates to a non-null, non-empty
	 * string.
	 * <p>
	 * Example:
	 * 
	 * <pre>
	 * a(style, styles("background-color", "red", "color", null, "display", "none"), $);
	 * 
	 * &lt;a style="background-color:red;display:none"&gt;&lt;/a&gt
	 * </pre>
	 * 
	 * @param style     first style may include a unit separated with a dot, which
	 *                  is appended to the value.
	 *                  <p>
	 *                  Example:
	 * 
	 *                  <pre>
	 *                  a(style, styles("height.px", 20), $);
	 *                  
	 *                  &lt;a style="height:20px"&gt;&lt;/a&gt
	 *                  </pre>
	 * 
	 * @param value     first value
	 * @param morePairs [String, Object] pairs
	 * @return style list
	 */
	public String styles(String style, Object value, Object... morePairs) {
		List<Object> all = new ArrayList<>();
		all.add(style);
		all.add(value);
		all.addAll(asList(flatten(morePairs)));

		Object[] pairs = checkPairs(mergeUnits(all.toArray()));

		StringBuilder sb = new StringBuilder();
		for (int i = 0, n = pairs.length; i < n; i += 2) {
			Object val = pairs[i + 1];
			if (val != null && !val.toString()
					.isEmpty()) {
				if (sb.length() > 0) {
					sb.append(';');
				}
				appendStyle((String) pairs[i], val, sb);
			}
		}
		return sb.toString();
	}

	private void appendStyle(String style, Object value, StringBuilder target) {
		int pos = style.lastIndexOf('.');
		String unit;
		if (pos < 0) {
			unit = "";
		} else {
			unit = style.substring(pos + 1);
			style = style.substring(0, pos);
		}

		target.append(style);
		target.append(':');
		target.append(value);
		target.append(unit);
	}

	@SuppressWarnings("unchecked")
	private T _this() {
		return (T) this;
	}
}
