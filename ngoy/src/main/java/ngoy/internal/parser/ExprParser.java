package ngoy.internal.parser;

import static java.lang.String.format;
import static java.util.Arrays.asList;
import static java.util.stream.Collectors.joining;

import java.io.StringReader;
import java.io.StringWriter;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.codehaus.commons.compiler.Location;
import org.codehaus.janino.Java.AmbiguousName;
import org.codehaus.janino.Java.Atom;
import org.codehaus.janino.Java.MethodInvocation;
import org.codehaus.janino.Java.Rvalue;
import org.codehaus.janino.Parser;
import org.codehaus.janino.Scanner;
import org.codehaus.janino.Unparser;
import org.codehaus.janino.util.AbstractTraverser;

import ngoy.core.NgoyException;
import ngoy.core.PipeTransform;
import ngoy.core.internal.Resolver;
import ngoy.internal.parser.org.springframework.expression.Expression;
import ngoy.internal.parser.org.springframework.expression.ExpressionType;
import ngoy.internal.parser.org.springframework.expression.ParserContext;
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
		try {
			Parser parser = new org.codehaus.janino.Parser(new Scanner(null, new StringReader(expr)));
			Atom atom = parser.parseExpression();

			new PrefixAdder(prefix, excludes).visitAtom(atom);

			StringWriter sw = new StringWriter();
			Unparser unparser = new Unparser(sw);
			unparser.unparseAtom(atom);
			unparser.close();

			return sw.toString();

		} catch (Exception e) {
			throw NgoyException.wrap(e);
		}
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

	private static class PrefixAdder extends AbstractTraverser<RuntimeException> {

		private static final Field MODIFIERS_FIELD;
		private static final Field IDENTIFIERS_FIELD;
		private static final Field N_FIELD;
		private static final Field OPTIONAL_TARGET_FIELD;

		static {
			try {
				IDENTIFIERS_FIELD = AmbiguousName.class.getField("identifiers");
				N_FIELD = AmbiguousName.class.getField("n");
				OPTIONAL_TARGET_FIELD = MethodInvocation.class.getField("optionalTarget");

				MODIFIERS_FIELD = Field.class.getDeclaredField("modifiers");
				MODIFIERS_FIELD.setAccessible(true);

				MODIFIERS_FIELD.setInt(OPTIONAL_TARGET_FIELD, OPTIONAL_TARGET_FIELD.getModifiers() & ~Modifier.FINAL);
				MODIFIERS_FIELD.setInt(IDENTIFIERS_FIELD, IDENTIFIERS_FIELD.getModifiers() & ~Modifier.FINAL);
				MODIFIERS_FIELD.setInt(N_FIELD, IDENTIFIERS_FIELD.getModifiers() & ~Modifier.FINAL);
			} catch (Exception e) {
				throw NgoyException.wrap(e);
			}
		}

		private final String prefix;
		private final Set<String> excludes;

		private PrefixAdder(String prefix, Set<String> excludes) {
			this.prefix = prefix;
			this.excludes = excludes;
		}

		private void insertPrefix(String prefix, AmbiguousName ambiguousName) {
			if (excludes.contains(ambiguousName.identifiers[0])) {
				return;
			}
			List<String> more = new ArrayList<>(asList(ambiguousName.identifiers));
			more.add(0, prefix);
			try {
				IDENTIFIERS_FIELD.set(ambiguousName, more.toArray(new String[more.size()]));
				N_FIELD.set(ambiguousName, ambiguousName.n + 1);
			} catch (Exception e) {
				throw NgoyException.wrap(e);
			}
		}

		public void traverseRvalue(Rvalue rv) {
			if (rv instanceof AmbiguousName) {
				insertPrefix(prefix, (AmbiguousName) rv);
			} else if (rv instanceof MethodInvocation) {
				MethodInvocation mi = (MethodInvocation) rv;
				if (mi.optionalTarget == null && !excludes.contains(mi.methodName)) {
					try {
						OPTIONAL_TARGET_FIELD.set(mi, new AmbiguousName(Location.NOWHERE, new String[] { prefix }));
					} catch (Exception e) {
						throw NgoyException.wrap(e);
					}
				}
			}
		}
	}

	public static class ExpressionWithPipesParser extends TemplateAwareExpressionParser {

		private final Resolver resolver;

		public ExpressionWithPipesParser(Resolver resolver) {
			this.resolver = resolver;
		}

		@Override
		protected Expression doParseExpression(String expressionString, ParserContext context) throws ParseException {
			String e = convertPipesToTransformCalls(expressionString, resolver);
			return new Expression(ExpressionType.EXPRESSION, e);
		}
	}

	interface TextHandler {
		void text(String text, boolean isExpr);
	}

	static void parse(String text, Resolver resolver, TextHandler handler) {
		acceptExpr(new ExpressionWithPipesParser(resolver).parseExpression(text, TEMPLATE_CONTEXT), handler);
	}

	private static void acceptExpr(Expression e, TextHandler handler) {
		switch (e.type) {
		case LITERAL:
			handler.text(e.string, false);
			break;
		case EXPRESSION:
			handler.text(e.string, true);
			break;
		case COMPOUND:
			for (Expression child : e.expressions) {
				acceptExpr(child, handler);
			}
			break;
		default:
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
