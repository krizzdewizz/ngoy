package ngoy.core;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Stream;

/**
 * Flattens structural items, such as an array, into a single list.
 * 
 * @author krizz
 */
public class FlatList {

	/**
	 * Flattens structural items, such as an array, into a single list.
	 * <ul>
	 * <li>If the item is an array, iterable or stream, adds those items to the
	 * list.</li>
	 * <li>If the item is a {@link Map} adds its entries to the list</li>
	 * <li>If the item is a {@link Map.Entry} adds its key and value to the
	 * list</li>
	 * </ul>
	 * Except for Map/Entry, the algorithm is perfomed recursively.
	 * <p>
	 * Example:
	 * 
	 * <pre>
	 * Map&lt;String, Object&gt; map = new HashMap&lt;&gt;();
	 * map.put(&quot;color&quot;, &quot;red&quot;);
	 * 
	 * assertThat(FlatList.flatten("a", map)).isEqualTo(new Object[] { "a", "color", "red" });
	 * </pre>
	 * 
	 * @author krizz
	 */
	public static Object[] flatten(Object... items) {
		List<Object> list = new ArrayList<>();
		for (Object it : items) {
			add(list, it);
		}
		return list.toArray();
	}

	private static void add(List<Object> list, Object it) {
		if (isArray(it)) {
			for (int i = 0, n = Array.getLength(it); i < n; i++) {
				add(list, Array.get(it, i));
			}
		} else if (it instanceof Iterable) {
			add(list, ((Iterable<?>) it).iterator());
		} else if (it instanceof Map) {
			for (Map.Entry<?, ?> e : ((Map<?, ?>) it).entrySet()) {
				add(list, e);
			}
		} else if (it instanceof Map.Entry) {
			Map.Entry<?, ?> e = (Entry<?, ?>) it;
			// do not flatten
			list.add(e.getKey());
			list.add(e.getValue());
		} else if (it instanceof Stream) {
			add(list, ((Stream<?>) it).iterator());
		} else {
			list.add(it); // may be null
		}
	}

	private static void add(List<Object> list, Iterator<?> iterator) {
		for (Iterator<?> iter = iterator; iter.hasNext();) {
			add(list, iter.next());
		}
	}

	private static boolean isArray(Object obj) {
		return obj != null && obj.getClass()
				.isArray();
	}
}
