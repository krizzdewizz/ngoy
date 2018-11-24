package ngoy.internal.script;

import static ngoy.core.NgoyException.wrap;

import java.io.BufferedReader;
import java.io.StringReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ngoy.core.NgoyException;
import ngoy.core.internal.Ctx;
import ngoy.core.internal.Resolver;
import ngoy.internal.parser.ExprParser;

public class NgoyScript {

	private static final Pattern LET_PATTERN = Pattern.compile("let\\s*(.*)\\s*=(.*)");
	private static final Pattern RETURN_PATTERN = Pattern.compile("return\\s*(.*)");

	private final Resolver resolver;

	public NgoyScript(Resolver resolver) {
		this.resolver = resolver;
	}

	public Object run(String script, Ctx ctx) {
		try (BufferedReader reader = new BufferedReader(new StringReader(script))) {
			Object result = null;
			String line;
			int lineNbr = 0;
			while ((line = reader.readLine()) != null) {
				lineNbr++;

				line = line.trim();
				if (line.isEmpty()) {
					continue;
				}

				Matcher letMatcher;
				Matcher returnMatcher;
				if ((letMatcher = LET_PATTERN.matcher(line)).find()) {
					String var = letMatcher.group(1)
							.trim();
					if (ctx.getVariables()
							.contains(var)) {
						throw new NgoyException("NgoyScript error on line %s: variable '%s' is already defined", lineNbr, var);
					}
					String expr = letMatcher.group(2);
					Object exprResult = eval(expr, ctx);
					ctx.variable(var, exprResult);
				} else if ((returnMatcher = RETURN_PATTERN.matcher(line)).find()) {
					result = ctx.eval(returnMatcher.group(1));
					break;
				} else {
					result = eval(line, ctx);
				}
			}

			return result;

		} catch (Exception e) {
			throw wrap(e);
		}
	}

	private Object eval(String expr, Ctx ctx) {
		String newExpr = resolver == null ? expr : ExprParser.convertPipesToTransformCalls(expr, resolver);
		return ctx.eval(newExpr);
	}
}
