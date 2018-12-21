package ngoy.internal.parser;

import static ngoy.core.Util.isSet;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import ngoy.core.NgoyException;
import ngoy.internal.parser.template.CodeBuilder;
import ngoy.internal.parser.template.Printer;

public class LambdaParser {

	public static class Param {
		public final String type;
		public final String name;

		public Param(String type, String name) {
			this.type = type;
			this.name = name;
		}
	}

	public static class Lambda {
		public final List<Param> params;
		public final String expression;

		public Lambda(List<Param> params, String expression) {
			this.params = params;
			this.expression = expression;
		}

		public String toAnonClass() {
			String base = "LAMBDA";
			Printer printer = new Printer();
			new CodeBuilder(printer) {
				protected void doCreate() {
					$$("new ", base, "(){public Object LAMBDA_METH(");
					boolean hadParam = false;
					for (Param p : params) {
						if (hadParam) {
							$$(",");
						}
						$$(p.type.isEmpty() ? "Object" : p.type, " ", p.name);
						hadParam = true;
					}
					$$(")");
					$$("{return ", expression, ";}");
					$$("}");
				}
			}.create();

			return printer.toString();
		}
	}

	public static String parse(String s) {
		int[] outPos = new int[1];
		int len = s.length();
		String parsed = s;
		while (outPos[0] >= 0 && outPos[0] < len) {
			parsed = parse(parsed, null, outPos);
		}

		return parsed;
	}

	protected static String parse(String s, Lambda[] outLambda, int[] outPos) {
		if (outPos != null) {
			outPos[0] = -1;
		}
		if (!isSet(s)) {
			return s;
		}
		int pos = s.indexOf("->");
		if (pos < 0) {
			return s;
		}

		int[] paramPos = new int[1];
		int[] bodyPos = new int[1];
		List<Param> params = parseParams(s, pos - 1, paramPos);
		String body = parseBody(s, pos + 2, bodyPos);
		Lambda lambda = new Lambda(params, body);
		if (outLambda != null) {
			outLambda[0] = lambda;
		}
		if (outPos != null) {
			outPos[0] = bodyPos[0];
		}
		return s.substring(0, paramPos[0]) + lambda.toAnonClass() + s.substring(bodyPos[0]);
	}

	private static enum BodyState {
		READ, LPAREN, RPAREN
	}

	private static String parseBody(String s, int pos, int[] outPos) {
		BodyState state = BodyState.READ;
		StringBuilder body = new StringBuilder();
		int parenCount = 1;
		loop: for (int n = s.length(); pos < n; pos++) {
			char c = s.charAt(pos);
			switch (state) {
			case READ:
				if (c == '(') {
					parenCount++;
				} else if (c == ')') {
					parenCount--;
					if (parenCount == 0) {
						break loop;
					}
				}
				body.append(c);
				break;
			default:
			}
		}

		outPos[0] = pos;

		return body.toString();
	}

	private static enum ParamState {
		INIT, RPAREN
	}

	private static List<Param> parseParams(String s, int pos, int[] outPos) {
		ParamState state = ParamState.INIT;
		StringBuilder param = new StringBuilder();
		List<String> params = new ArrayList<>();
		StringBuilder initSb = new StringBuilder();
		loop: for (; pos >= 0; pos--) {
			char c = s.charAt(pos);
			switch (state) {
			case INIT:
				if (c == ')') {
					state = ParamState.RPAREN;
				} else if (c == '(') {
					break loop;
				} else {
					initSb.append(c);
				}
				break;
			case RPAREN:
				if (c == '(') {
					params.add(reverse(param.toString()));
					param.setLength(0);
					state = ParamState.INIT;
					break loop;
				} else if (c == ',') {
					params.add(reverse(param.toString()));
					param.setLength(0);
				} else {
					param.append(c);
				}

				break;
			}
		}

		if (state != ParamState.INIT) {
			throw new NgoyException("Malformed lambda expression: %s", s);
		}

		outPos[0] = pos == 0 ? 0 : pos + 1;

		List<Param> all = new ArrayList<>();

		if (params.isEmpty()) {
			String id = initSb.toString()
					.trim();
			if (!id.isEmpty()) {
				all.add(new Param("", reverse(id)));
			}
		} else {

			Collections.reverse(params);

			for (String p : params) {
				p = p.trim();
				if (p.isEmpty()) {
					continue;
				}
				String[] splits = p.split(" ");
				String type = splits.length > 1 ? splits[0].trim() : "";
				String name = splits.length > 1 ? splits[1].trim() : splits[0].trim();
				all.add(new Param(type, name));
			}
		}

		return all;
	}

	private static String reverse(String s) {
		StringBuilder sb = new StringBuilder();
		for (int i = s.length() - 1; i >= 0; i--) {
			sb.append(s.charAt(i));
		}
		return sb.toString();
	}
}