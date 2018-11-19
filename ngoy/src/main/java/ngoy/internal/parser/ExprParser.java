package ngoy.internal.parser;

import static java.lang.String.format;
import static java.util.stream.Collectors.joining;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.expression.Expression;
import org.springframework.expression.ParserContext;
import org.springframework.expression.common.CompositeStringExpression;
import org.springframework.expression.common.LiteralExpression;
import org.springframework.expression.common.TemplateParserContext;
import org.springframework.expression.spel.standard.SpelExpression;
import org.springframework.expression.spel.standard.SpelExpressionParser;

import ngoy.core.NgoyException;
import ngoy.core.PipeTransform;
import ngoy.core.internal.Ctx;
import ngoy.core.internal.Resolver;

public class ExprParser {

	static final TemplateParserContext TEMPLATE_CONTEXT = new TemplateParserContext("{{", "}}");

	private static class ExpressionWithPipesParser extends SpelExpressionParser {
		private static final Pattern PIPE_PATTERN = Pattern.compile("([^\\|]+)");
		private final Resolver resolver;

		public ExpressionWithPipesParser(Resolver resolver) {
			this.resolver = resolver;
		}

		@Override
		protected SpelExpression doParseExpression(String expressionString, ParserContext context) throws ParseException {
			String e = convertPipesToTransformCalls(expressionString);
			return super.doParseExpression(e, context);
		}

		private String convertPipesToTransformCalls(String expressionString) {
			Matcher matcher = PIPE_PATTERN.matcher(expressionString);
			matcher.find();
			String exprHead = matcher.group(1)
					.trim();
			String e = exprHead;
			while (matcher.find()) {
				String pipe = matcher.group(1)
						.trim();

				List<String> pipeParams = new ArrayList<>();
				pipe = PipeParser.parsePipe(pipe, pipeParams);
				String params = pipeParams.stream()
						.collect(joining(","));

				Class<?> resolvedPipe = resolver.resolvePipe(pipe);
				if (resolvedPipe == null) {
					throw new ParseException("Pipe not found for name '%s'", pipe);
				} else if (!PipeTransform.class.isAssignableFrom(resolvedPipe)) {
					throw new ParseException("Pipe %s must implement %s", resolvedPipe.getName(), PipeTransform.class.getName());
				}

				e = format("%s.pipe(\"%s\").transform(%s%s)", Ctx.CTX_VARIABLE, resolvedPipe.getName(), e, params.isEmpty() ? "" : format(",%s", params));
			}
			return e;
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
}
