package ngoy.internal.parser;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ExprParser {

	interface TextHandler {
		void text(String text, boolean isExpr);
	}

	private static final Pattern EXPR_PATTERN = Pattern.compile("\\{\\{(.*?)\\}\\}", Pattern.MULTILINE);

	static void parse(String text, TextHandler handler) {
		Matcher matcher = EXPR_PATTERN.matcher(text);
		int last = 0;
		while (matcher.find()) {
			String expr = matcher.group(1);
			String left = text.substring(last, matcher.start());
			if (!left.isEmpty()) {
				handler.text(left, false);
			}
			handler.text(expr, true);
			last = matcher.end();
		}

		if (last < text.length()) {
			String s = text.substring(last);
			if (!s.isEmpty()) {
				handler.text(s, false);
			}
		}
	}
}
