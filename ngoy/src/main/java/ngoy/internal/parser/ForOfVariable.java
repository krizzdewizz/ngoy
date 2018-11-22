package ngoy.internal.parser;

import static ngoy.core.XDom.attributes;

import java.util.EnumMap;
import java.util.Map;

import jodd.jerry.Jerry;
import jodd.lagarto.dom.Attribute;
import ngoy.core.NgoyException;

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

	public static boolean parse(Jerry el, ParseElement onParse) {
		String ngForOf = el.attr("[ngForOf]");
		if (ngForOf == null) {
			return false;
		}

		Map<ForOfVariable, String> map = new EnumMap<>(ForOfVariable.class);
		String itemName = null;
		for (Attribute attr : attributes(el)) {
			String name = attr.getName();
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
