package org.ngoy.internal.parser;

import java.util.EnumMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.ngoy.core.NgoyException;

public enum ForOfVariable {
	index, first, last, even, odd;

	private static final Pattern FOR_OF_PATTERN = Pattern.compile("let\\s*(.*)\\s*of\\s*(.*)");
	private static final Pattern VAR_DECL_PATTERN = Pattern.compile("(.*)\\s*as\\s*(.*)");

	public static String[] parseNgFor(String expr) {
		Matcher matcher = FOR_OF_PATTERN.matcher(expr);
		if (!matcher.find()) {
			throw new ParseException("*ngFor expression malformed: %s", expr);
		}

		String itemName = matcher.group(1)
				.trim();
		String listName = matcher.group(2)
				.trim();

		int semi = listName.indexOf(';');
		if (semi > -1) {
			listName = listName.substring(0, semi);
		}

		return new String[] { itemName, listName };
	}

	public static Map<ForOfVariable, String> parse(String ngFor) {
		Map<ForOfVariable, String> result = new EnumMap<>(ForOfVariable.class);

		int semi = ngFor.indexOf(';');
		if (semi < 0) {
			return result;
		}

		String right = ngFor.substring(semi)
				.trim();

		String[] parts = right.split(";");
		for (String part : parts) {
			part = part.trim();
			if (part.isEmpty()) {
				continue;
			}
			Matcher matcher = VAR_DECL_PATTERN.matcher(part);
			if (!matcher.find()) {
				throw new NgoyException("Parse error in ngFor. Variables must be specified like: 'index as i; odd as o'");
			}

			String variable = matcher.group(1)
					.trim();
			ForOfVariable fv;
			try {
				fv = ForOfVariable.valueOf(variable);
			} catch (Exception e) {
				throw new NgoyException("Unknown ngFor variable: %s", variable);
			}
			String alias = matcher.group(2)
					.trim();
			result.put(fv, alias);
		}

		return result;
	}

	public static Map<ForOfVariable, String> valueOf(String[] pairs) {
		Map<ForOfVariable, String> map = new EnumMap<>(ForOfVariable.class);
		for (int i = 0, n = pairs.length; i < n; i += 2) {
			map.put(ForOfVariable.valueOf(pairs[i]), pairs[i + 1]);
		}
		return map;
	}
}
