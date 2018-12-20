package ngoy.internal.parser;

import static java.lang.String.format;
import static java.util.stream.Collectors.joining;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.BodyDeclaration;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.expr.ArrayCreationExpr;
import com.github.javaparser.ast.expr.FieldAccessExpr;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.stmt.ReturnStmt;
import com.github.javaparser.ast.visitor.GenericVisitorAdapter;
import com.github.javaparser.resolution.types.ResolvedArrayType;
import com.github.javaparser.symbolsolver.JavaSymbolSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.CombinedTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.ReflectionTypeSolver;

import ngoy.core.NgoyException;
import ngoy.core.PipeTransform;
import ngoy.core.Util;
import ngoy.core.Variable;
import ngoy.core.internal.Resolver;
import ngoy.internal.parser.org.springframework.expression.Expression;
import ngoy.internal.parser.org.springframework.expression.ExpressionType;
import ngoy.internal.parser.org.springframework.expression.ParserContext;
import ngoy.internal.parser.org.springframework.expression.TemplateAwareExpressionParser;
import ngoy.internal.parser.org.springframework.expression.TemplateParserContext;

public class ExprParser {

	static {
		CombinedTypeSolver combinedTypeSolver = new CombinedTypeSolver();
		combinedTypeSolver.add(new ReflectionTypeSolver());

		// Configure JavaParser to use type resolution
		JavaSymbolSolver symbolSolver = new JavaSymbolSolver(combinedTypeSolver);
		JavaParser.getStaticConfiguration()
				.setSymbolResolver(symbolSolver);
	}

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

	public static String prefixName(Class<?> clazz, Map<String, Class<?>> prefixes, String expr, String prefix, Set<String> excludes, Map<String, Variable<?>> variables, ClassDef[] outLastClassDef) {
		try {
//			expr = fieldAccessToGetter(clazz, prefixes, expr, variables, outLastClassDef);

			CompilationUnit ex = JavaParser.parse(format("class X extends %s { void foo() {  return %s ;  }  }", Util.sourceClassName(clazz), expr));

			ClassOrInterfaceDeclaration[] c = new ClassOrInterfaceDeclaration[1];

			ex.accept(new GenericVisitorAdapter<Void, Void>() {

				@Override
				public Void visit(ClassOrInterfaceDeclaration n, Void arg) {
					c[0] = n;
					return super.visit(n, arg);
				}

				@Override
				public Void visit(ArrayCreationExpr n, Void arg) {
					try {
						ResolvedArrayType at = n.calculateResolvedType()
								.asArrayType();

						String arrayClass = Util.getArrayClass(at.getComponentType()
								.describe());
						outLastClassDef[0] = ClassDef.of(Class.forName(arrayClass));
					} catch (ClassNotFoundException e) {
						e.printStackTrace();
					}
					return super.visit(n, arg);
				}

				@Override
				public Void visit(MethodCallExpr n, Void arg) {

					com.github.javaparser.ast.expr.Expression scope = n.getScope()
							.orElse(null);

					if (scope == null && !excludes.contains(n.getNameAsString())) {
						n.setScope(new NameExpr(prefix));
					}

					return super.visit(n, arg);
				}

				@Override
				public Void visit(FieldAccessExpr n, Void arg) {

					return super.visit(n, arg);
				}

				@Override
				public Void visit(NameExpr n, Void arg) {

					try {
						n.calculateResolvedType();
						return super.visit(n, arg);
					} catch (Exception e) {
					}

					n.setName("getName()");

					String name = n.getNameAsString();
					BodyDeclaration<?> getter = c[0].getMembers()
							.stream()
							.filter(it -> it.isMethodDeclaration() && it.asMethodDeclaration()
									.getNameAsString()
									.equals("getName"))
							.findFirst()
							.orElse(null);

					if (!excludes.contains(name) && !name.startsWith(prefix)) {
						n.setName(format("%s.%s", prefix, name));
					}

					return super.visit(n, arg);
				}
			}, null);

			return ex.findFirst(ReturnStmt.class)
					.map(rs -> rs.getExpression()
							.get())
					.get()
					.toString();

		}
//		catch (CompileException e) {
//			throw new NgoyException("Compile error: %s", e);
//		}
		catch (Exception e) {
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
