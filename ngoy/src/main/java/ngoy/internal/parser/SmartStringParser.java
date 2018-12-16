package ngoy.internal.parser;

/**
 * Converts a single or double quoted string to a double quoted java string.
 * 
 * @author krizz
 */
public final class SmartStringParser {

	private SmartStringParser() {
	}

	private static enum State {
		INIT, SINGLE, DOUBLE;
	}

	/**
	 * Converts a single or double quoted string to a double quoted java string.
	 * 
	 * @param s single or double quoted string
	 * @return double quoted string
	 */
	public static String toJavaString(String s) {
		if (s == null || s.isEmpty()) {
			return s;
		}

		State state = State.INIT;

		StringBuilder sb = new StringBuilder();

		for (int i = 0, n = s.length(); i < n; i++) {
			char c = s.charAt(i);
			char next = i + 1 < n ? s.charAt(i + 1) : 0;

			switch (state) {
			case INIT:
				if (c == '\'') {
					state = State.SINGLE;
					sb.append('"');
				} else if (c == '"') {
					state = State.DOUBLE;
					sb.append(c);
				} else {
					sb.append(c);
				}
				break;
			case SINGLE:
				if (c == '\\' && next == '\'') {
					sb.append("\\\"");
					i += 1;
					if (i + 1 == n) {
						state = State.INIT;
					}
				} else if (c == '\'') {
					sb.append('"');
					state = State.INIT;
				} else if (c == '"') {
					sb.append("\\\"");
				} else {
					sb.append(c);
				}
				break;
			case DOUBLE:
				if (c == '\\' && next == '"') {
					sb.append("\\\"");
					i += 1;
					if (i + 1 == n) {
						state = State.INIT;
					}
				} else if (c == '"') {
					sb.append('"');
					state = State.INIT;
				} else {
					sb.append(c);
				}
				break;
			}
		}

		if (state != State.INIT) {
			throw new ParseException("String literal is not properly closed with a %s-quote: ", state.name()
					.toLowerCase());
		}

		return sb.toString();
	}
}
