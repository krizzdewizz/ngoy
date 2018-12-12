package ngoy.internal.parser;

import static java.lang.String.format;
import static java.util.stream.Collectors.joining;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.expr.FieldAccessExpr;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.SimpleName;

import ngoy.core.NgoyException;
import ngoy.core.PipeTransform;
import ngoy.core.internal.Resolver;
import ngoy.internal.parser.org.springframework.expression.CompositeStringExpression;
import ngoy.internal.parser.org.springframework.expression.Expression;
import ngoy.internal.parser.org.springframework.expression.LiteralExpression;
import ngoy.internal.parser.org.springframework.expression.ParserContext;
import ngoy.internal.parser.org.springframework.expression.SpelExpression;
import ngoy.internal.parser.org.springframework.expression.TemplateAwareExpressionParser;
import ngoy.internal.parser.org.springframework.expression.TemplateParserContext;

public class ExprParser {

	static final TemplateParserContext TEMPLATE_CONTEXT = new TemplateParserContext("{{", "}}");

	private static final Pattern PIPE_PATTERN = Pattern.compile("([^\\|]+)");

	// cheap hack to prevent || from matching the pattern
	private static final String OR_ESCAPE = "☼♫";

	private static String escapeOr(String s) {
		return s.replace("||", OR_ESCAPE);
	}

	private static String unescapeOr(String s) {
		return s.replace(OR_ESCAPE, "||");
	}

	public static String prefixName(String expr, String prefix, Set<String> excludes) {
		com.github.javaparser.ast.expr.Expression ex = JavaParser.parseExpression(expr);
		ex.findAll(SimpleName.class)
				.stream()
				.filter(simpleName -> !excludes.contains(simpleName.getIdentifier()))
				.forEach(simpleName -> {
					Node parent = simpleName.getParentNode()
							.get();
					boolean doIt = true;
					if (parent instanceof MethodCallExpr) {
						MethodCallExpr methodCall = (MethodCallExpr) parent;
						doIt = !methodCall.getScope()
								.isPresent();
					} else if (parent instanceof FieldAccessExpr) {
						doIt = false;
					}
					if (doIt) {
//						simpleName.setIdentifier(format("_cmp.%s", convertFieldAccess(getters, simpleName.getIdentifier())));
						simpleName.setIdentifier(format("%s.%s", prefix, simpleName.getIdentifier()));
					}
				});
		return ex.toString();
	}

	public static String convertPipesToTransformCalls(String expressionString, Resolver resolver) {

		Matcher matcher = PIPE_PATTERN.matcher(escapeOr(expressionString));
		matcher.find();
		String exprHead = matcher.group(1)
				.trim();
		String e = unescapeOr(exprHead);
		while (matcher.find()) {
			String pipe = matcher.group(1)
					.trim();

			List<String> pipeParams = new ArrayList<>();
			pipe = parsePipe(pipe, pipeParams);
			String params = unescapeOr(pipeParams.stream()
					.collect(joining(",")));

			Class<?> resolvedPipe = resolver.resolvePipe(pipe);
			if (resolvedPipe == null) {
				throw new ParseException("Pipe not found for name '%s'", pipe);
			} else if (!PipeTransform.class.isAssignableFrom(resolvedPipe)) {
				throw new ParseException("Pipe %s must implement %s", resolvedPipe.getName(), PipeTransform.class.getName());
			}

			e = format("$%s(%s%s)", pipe, e, params.isEmpty() ? "" : format(",%s", params));
		}
		return e;
	}

	public static class ExpressionWithPipesParser extends TemplateAwareExpressionParser {

		private final Resolver resolver;

		public ExpressionWithPipesParser(Resolver resolver) {
			this.resolver = resolver;
		}

		@Override
		protected Expression doParseExpression(String expressionString, ParserContext context) throws ParseException {
			String e = convertPipesToTransformCalls(expressionString, resolver);
			return new SpelExpression(e);
		}
	}

	interface TextHandler {
		void text(String text, boolean isExpr);
	}

	static void parse(String text, Resolver resolver, TextHandler handler) {
		acceptExpr(new ExpressionWithPipesParser(resolver).parseExpression(text, TEMPLATE_CONTEXT), handler);
	}

	private static void acceptExpr(Expression e, TextHandler handler) {
		if (e instanceof LiteralExpression) {
			handler.text(e.getExpressionString(), false);
		} else if (e instanceof SpelExpression) {
			handler.text(e.getExpressionString(), true);
		} else if (e instanceof CompositeStringExpression) {
			CompositeStringExpression compositeExpr = (CompositeStringExpression) e;
			for (Expression child : compositeExpr.getExpressions()) {
				acceptExpr(child, handler);
			}
		} else {
			throw new NgoyException("Unknown expression type: %s", e.getClass()
					.getName());
		}
	}

	private static String parsePipe(String pipe, List<String> targetParams) {
		int pos = pipe.indexOf(':');
		if (pos < 0) {
			return pipe;
		}

		parsePipeParams(pipe.substring(pos + 1), targetParams::add);
		return pipe.substring(0, pos);
	}

	private static void parsePipeParams(String s, Consumer<String> param) {
		StringBuilder result = new StringBuilder();

		boolean quote = false;
		for (int i = 0, n = s.length(); i < n; i++) {
			char c = s.charAt(i);
			boolean add = true;
			if (c == '\'' || c == '"') {
				quote = !quote;
			} else if (c == ':' && !quote) {
				param.accept(result.toString());
				result.setLength(0);
				add = false;
			}
			if (add) {
				result.append(c);
			}
		}

		if (result.length() > 0) {
			param.accept(result.toString());
		}
	}

}
