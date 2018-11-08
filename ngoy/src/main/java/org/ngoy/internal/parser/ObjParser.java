package org.ngoy.internal.parser;

import static java.util.Collections.emptyMap;
import static java.util.stream.Collectors.toMap;

import java.util.Map;
import java.util.stream.Stream;

public class ObjParser {
	
	private ObjParser() {
	}

	public static Map<String, String> parse(String s) {
		s = s.trim();
		if (s.isEmpty()) {
			return emptyMap();
		}

		if (!s.startsWith("{") || !s.endsWith("}")) {
			throw new ParseException("malformed object literal: %s", s);
		}

		String obj = s.substring(1, s.length() - 1);

		String[] lines = obj.split(",");
		return Stream.of(lines)
				.map(String::trim)
				.filter(line -> !line.isEmpty())
				.map(line -> line.split(":"))
				.peek(ObjParser::trimPair)
				.collect(toMap(pair -> pair[0], pair -> pair[1]));
	}

	private static void trimPair(String[] pair) {
		String key = pair[0].trim();
		if (key.charAt(0) == '\'' || key.charAt(0) == '"') {
			key = key.substring(1, key.length() - 1);
		}
		pair[0] = key;
		pair[1] = pair[1].trim();
	}
}
