package ngoy.internal.parser;

import static java.lang.String.format;
import static java.util.Arrays.asList;
import static java.util.stream.Collectors.joining;
import static ngoy.internal.parser.FieldAccessToGetterParser.fieldAccessToGetter;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.codehaus.commons.compiler.CompileException;
import org.codehaus.janino.Java.AmbiguousName;
import org.codehaus.janino.Java.FunctionDeclarator.FormalParameter;
import org.codehaus.janino.Java.Lvalue;
import org.codehaus.janino.Java.MethodDeclarator;
import org.codehaus.janino.Java.MethodInvocation;
import org.codehaus.janino.Java.NewAnonymousClassInstance;
import org.codehaus.janino.Java.Rvalue;
import org.codehaus.janino.util.DeepCopier;

import ngoy.core.NgoyException;
import ngoy.core.PipeTransform;
import ngoy.core.Variable;
import ngoy.core.internal.Resolver;
import ngoy.internal.parser.org.springframework.expression.Expression;
import ngoy.internal.parser.org.springframework.expression.ExpressionType;
import ngoy.internal.parser.org.springframework.expression.ParserContext;
import ngoy.internal.parser.org.springframework.expression.TemplateAwareExpressionParser;
import ngoy.internal.parser.org.springframework.expression.TemplateParserContext;
import ngoy.internal.parser.template.JavaTemplate;

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

	public static String prefixName(Class<?> clazz, Map<String, Class<?>> prefixes, String expr, String prefix, Set<String> excludes, Map<String, Variable<?>> variables, ClassDef[] outLastClassDef,
			Set<String> outMethodCalls) {
		Set<String> outCalls = outMethodCalls != null ? outMethodCalls : new HashSet<>();
		return fieldAccessToGetter(clazz, prefixes, expr, variables, rvalue -> new Prefixer(prefix, excludes, outCalls).copyRvalue(rvalue), outLastClassDef);
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

	private static class Prefixer extends DeepCopier {
		private final String prefix;
		private final Set<String> outMethodCalls;
		private Set<String> excludes;

		private Prefixer(String prefix, Set<String> excludes, Set<String> outMethodCalls) {
			this.prefix = prefix;
			this.excludes = excludes;
			this.outMethodCalls = outMethodCalls;
		}

		boolean inAnon;

		@Override
		public Rvalue copyNewAnonymousClassInstance(NewAnonymousClassInstance subject) throws CompileException {
			inAnon = true;
			Rvalue anon = super.copyNewAnonymousClassInstance(subject);
			inAnon = false;
			return anon;
		}

		@Override
		public MethodDeclarator copyMethodDeclarator(MethodDeclarator subject) throws CompileException {
			Set<String> prevExcludes;
			if (inAnon) {
				prevExcludes = new HashSet<>(excludes);
				for (FormalParameter p : subject.formalParameters.parameters) {
					excludes.add(p.name);
					excludes.add(p.name.substring(1)); // remove _
				}
			} else {
				prevExcludes = excludes;
			}
			MethodDeclarator md = super.copyMethodDeclarator(subject);
			excludes = prevExcludes;
			return md;
		}

		@Override
		public Lvalue copyAmbiguousName(AmbiguousName subject) throws CompileException {
			if (excludes.contains(subject.identifiers[0]) || prefix.isEmpty()) {
				return super.copyAmbiguousName(subject);
			}

			List<String> more = new ArrayList<>(asList(subject.identifiers));
			more.add(0, prefix);

			return new AmbiguousName(subject.getLocation(), more.toArray(new String[more.size()]), subject.n + 1);
		}

		@Override
		public Rvalue copyMethodInvocation(MethodInvocation subject) throws CompileException {
			outMethodCalls.add(subject.methodName);
			if (subject.optionalTarget == null && !excludes.contains(subject.methodName) && !prefix.isEmpty()) {
				return new MethodInvocation(subject.getLocation(), new AmbiguousName(subject.getLocation(), new String[] { prefix }), subject.methodName, copyRvalues(subject.arguments));
			} else if (subject.methodName.equals("$raw")) {
				return new MethodInvocation(subject.getLocation(), null, subject.methodName,
						new Rvalue[] { copyRvalue(subject.arguments[0]), new AmbiguousName(subject.getLocation(), new String[] { JavaTemplate.CTX_VAR }) });
			}

			return super.copyMethodInvocation(subject);
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
			throw new NgoyException("Unknown expression type: %s", e.type);
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
