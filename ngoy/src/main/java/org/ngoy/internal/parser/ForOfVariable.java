package org.ngoy.internal.parser;

import java.util.EnumMap;
import java.util.Map;

import org.jsoup.nodes.Attribute;
import org.jsoup.nodes.Element;
import org.ngoy.core.NgoyException;

public enum ForOfVariable {
	index, first, last, even, odd;

	public static Map<ForOfVariable, String> valueOf(String[] pairs) {
		Map<ForOfVariable, String> map = new EnumMap<>(ForOfVariable.class);
		for (int i = 0, n = pairs.length; i < n; i += 2) {
			map.put(ForOfVariable.valueOf(pairs[i]), pairs[i + 1]);
		}
		return map;
	}

	public interface ParseElement {
		void run(String[] itemAndListName, Map<ForOfVariable, String> variables);
	}

	public static boolean parse(Element el, ParseElement onParse) {
		String ngForOf = el.attr("[ngForOf]");
		if (ngForOf.isEmpty()) {
			return false;
		}

		Map<ForOfVariable, String> map = new EnumMap<>(ForOfVariable.class);

		String itemName = null;
		for (Attribute attr : el.attributes()) {
			String name = attr.getKey();
			if (!name.startsWith("let-")) {
				continue;
			}
			String varName = name.substring("let-".length());
			String value = attr.getValue();
			if (value == null) {
				itemName = varName;
			} else {
				ForOfVariable fv;
				try {
					fv = ForOfVariable.valueOf(value);
				} catch (Exception e) {
					throw new NgoyException("Unknown ngFor variable: %s", value);
				}
				map.put(fv, varName);
			}
		}

		onParse.run(new String[] { itemName, ngForOf }, map);
		return true;
	}
}
