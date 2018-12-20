package ngoy.internal.parser;

import java.lang.reflect.Method;
import java.util.EnumSet;

import org.junit.Test;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.BodyDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.expr.LambdaExpr;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.ObjectCreationExpr;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.ExpressionStmt;
import com.github.javaparser.ast.stmt.ReturnStmt;
import com.github.javaparser.ast.stmt.Statement;
import com.github.javaparser.resolution.types.ResolvedType;
import com.github.javaparser.symbolsolver.JavaSymbolSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.CombinedTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.ReflectionTypeSolver;

public class JavaParserTest {

	void foo() {
		java.util.stream.Stream.of(1)
				.map(new java.util.function.Function() {

					public java.lang.Object apply(java.lang.Object s) {
						return s;
					}
				});
	}

	@Test
	public void test() {
		CombinedTypeSolver combinedTypeSolver = new CombinedTypeSolver();
		combinedTypeSolver.add(new ReflectionTypeSolver());

		// Configure JavaParser to use type resolution
		JavaSymbolSolver symbolSolver = new JavaSymbolSolver(combinedTypeSolver);
		JavaParser.getStaticConfiguration()
				.setSymbolResolver(symbolSolver);

		CompilationUnit cu = JavaParser.parse("class X { void foo() { java.util.stream.Stream.of(1).map(s -> s); } } ");

		cu.findAll(LambdaExpr.class)
				.forEach(l -> {

					ResolvedType resolvedType = l.calculateResolvedType();

					MethodCallExpr mce = ((MethodCallExpr) l.getParentNode()
							.get());

					mce.getScope()
							.get()
							.calculateResolvedType();

					String fun = resolvedType.asReferenceType()
							.getId();

					try {
						Class<?> funClass = Class.forName(fun);
						Method m0 = funClass.getMethods()[0];

						ObjectCreationExpr oc = new ObjectCreationExpr(null, JavaParser.parseClassOrInterfaceType(fun), new NodeList<>());

						NodeList<BodyDeclaration<?>> nl = new NodeList<>();
						MethodDeclaration md = new MethodDeclaration(EnumSet.of(Modifier.PUBLIC), JavaParser.parseClassOrInterfaceType(m0.getReturnType()
								.getName()), m0.getName());

						final NodeList<Parameter> parameters = new NodeList<>();

						NodeList<Parameter> lambdaParams = l.getParameters();
						Class<?>[] parameterTypes = m0.getParameterTypes();
						for (int i = 0, n = lambdaParams.size(); i < n; i++) {
							Parameter lp = lambdaParams.get(i);

							parameters.add(new Parameter(JavaParser.parseClassOrInterfaceType(parameterTypes[i].getName()), lp.getName()
									.toString()));
						}

						md.setParameters(parameters);

						NodeList<Statement> blockList = new NodeList<>();
						ExpressionStmt lambdaBody = (ExpressionStmt) l.getBody();
						blockList.add(new ReturnStmt(lambdaBody.getExpression()));
						md.setBody(new BlockStmt(blockList));
						nl.add(md);
						oc.setAnonymousClassBody(nl);

						l.replace(oc);
					} catch (ClassNotFoundException e) {
						e.printStackTrace();
					}

				});

		System.out.println(cu);

	}
}
