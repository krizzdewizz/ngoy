package ngoy.internal.parser;

import static java.lang.String.format;
import static java.util.Arrays.asList;
import static java.util.stream.Collectors.joining;
import static ngoy.core.NgoyException.wrap;
import static ngoy.core.Util.findMethod;
import static ngoy.core.Util.getCompileExceptionMessageWithoutLocation;
import static ngoy.core.Util.isSet;
import static ngoy.core.Util.sourceClassName;
import static ngoy.core.Util.tryLoadClass;

import java.io.StringReader;
import java.io.StringWriter;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

import org.codehaus.commons.compiler.CompileException;
import org.codehaus.commons.compiler.Location;
import org.codehaus.janino.Java;
import org.codehaus.janino.Java.AmbiguousName;
import org.codehaus.janino.Java.AnonymousClassDeclaration;
import org.codehaus.janino.Java.ArrayAccessExpression;
import org.codehaus.janino.Java.ArrayInitializerOrRvalue;
import org.codehaus.janino.Java.ArrayType;
import org.codehaus.janino.Java.Assignment;
import org.codehaus.janino.Java.Atom;
import org.codehaus.janino.Java.BinaryOperation;
import org.codehaus.janino.Java.BlockStatement;
import org.codehaus.janino.Java.BooleanLiteral;
import org.codehaus.janino.Java.Cast;
import org.codehaus.janino.Java.CharacterLiteral;
import org.codehaus.janino.Java.Crement;
import org.codehaus.janino.Java.FieldAccessExpression;
import org.codehaus.janino.Java.FloatingPointLiteral;
import org.codehaus.janino.Java.FunctionDeclarator.FormalParameter;
import org.codehaus.janino.Java.FunctionDeclarator.FormalParameters;
import org.codehaus.janino.Java.IntegerLiteral;
import org.codehaus.janino.Java.LocalVariableDeclarationStatement;
import org.codehaus.janino.Java.Lvalue;
import org.codehaus.janino.Java.MethodDeclarator;
import org.codehaus.janino.Java.MethodInvocation;
import org.codehaus.janino.Java.NewAnonymousClassInstance;
import org.codehaus.janino.Java.NewClassInstance;
import org.codehaus.janino.Java.NewInitializedArray;
import org.codehaus.janino.Java.NullLiteral;
import org.codehaus.janino.Java.ParenthesizedExpression;
import org.codehaus.janino.Java.QualifiedThisReference;
import org.codehaus.janino.Java.ReferenceType;
import org.codehaus.janino.Java.ReturnStatement;
import org.codehaus.janino.Java.Rvalue;
import org.codehaus.janino.Java.StringLiteral;
import org.codehaus.janino.Java.ThisReference;
import org.codehaus.janino.Java.VariableDeclarator;
import org.codehaus.janino.Mod;
import org.codehaus.janino.Parser;
import org.codehaus.janino.Scanner;
import org.codehaus.janino.Unparser;
import org.codehaus.janino.util.DeepCopier;

import ngoy.core.NgoyException;
import ngoy.core.Nullable;
import ngoy.core.Util;
import ngoy.core.Variable;

/**
 * Converts a field access to a getter call.
 * <p>
 * This is the beast.
 * <ul>
 * <li>converts smart strings first</li>
 * <li>converts field access to getter</li>
 * <li>convert array subscript on java.util.List and java.util.Map to get()
 * calls</li>
 * <li>since janino does not support generics, inserts the necessary cast
 * expressions</li>
 * <li>infers the type of the expression so we can have 'let/var' in *ngFor</li>
 * </ul>
 * 
 * @author krizz
 */
public final class FieldAccessToGetterParser {

	private static class AtomDef<T> {
		private T atom;
		private ClassDef classDef;

		public AtomDef(T atom, ClassDef classDef) {
			this.atom = atom;
			this.classDef = classDef;
		}
	}

	private static Rvalue unwrapParenthesizedExpression(Rvalue atom) {
		return atom instanceof ParenthesizedExpression ? ((ParenthesizedExpression) atom).value : atom;
	}

	private static Method findGetter(Class<?> clazz, String fieldName) {
		String right = fieldName.substring(0, 1)
				.toUpperCase() + fieldName.substring(1);
		Method meth;
		if ((meth = findMethod(clazz, format("get%s", right), 0)) != null) {
			return meth;
		}
		if ((meth = findMethod(clazz, format("is%s", right), 0)) != null) {
			return meth;
		}

		return null;
	}

	private static class ToGetter extends DeepCopier {
		private final Map<String, Class<?>> prefixes;
		private final Map<String, Variable<?>> variables;
		private ClassDef lastClassDef;

		public ToGetter(Class<?> clazz, Map<String, Class<?>> prefixes, Map<String, Variable<?>> variables) {
			this.variables = variables;
			this.prefixes = prefixes;
			lastClassDef = ClassDef.of(clazz);
		}

		private AtomDef<AmbiguousName> toGetter(Class<?> clazz, AmbiguousName ambiguousName) {
			String[] ids = ambiguousName.identifiers;

			if (ids[0].startsWith("java")) {
				String className = Arrays.asList(ids)
						.subList(0, ambiguousName.n)
						.stream()
						.collect(joining("."));
				Class<?> c = tryLoadClass(className);
				return new AtomDef<>(ambiguousName, ClassDef.of(c != null ? c : clazz));
			}

			Class<?> prefix = prefixes.get(ids[0]);

			ClassDef cd = null;
			int startIndex;
			if (prefix != null) {
				cd = ClassDef.of(prefix);
				startIndex = 1;
			} else {
				cd = ClassDef.of(clazz);
				startIndex = 0;
			}

			String[] idsCopy = new String[ids.length];
			System.arraycopy(ids, 0, idsCopy, 0, ids.length);

			for (int i = startIndex, n = ambiguousName.n; i < n; i++) {
				String id = ids[i];

				try {
					cd = ClassDef.of(cd.clazz.getField(id));
					continue;
				} catch (NoSuchFieldException e) {
					// ignore, try getter
				} catch (SecurityException e) {
					throw wrap(e);
				}

				Method getter = findGetter(cd.clazz, id);

				if (getter != null) {
					idsCopy[i] = format("%s()", getter.getName());
					cd = ClassDef.of(getter);
				} else {
					Variable<?> variable = variables.get(id);
					if (variable != null) {
						cd = ClassDef.of(variable.type);
					}
				}
			}

			return new AtomDef<>(new AmbiguousName(ambiguousName.getLocation(), idsCopy, ambiguousName.n), cd);
		}

		private AtomDef<Rvalue> toGetter(Class<?> clazz, ArrayAccessExpression aae) throws CompileException {
			ClassDef cd = ClassDef.of(clazz);
			Atom target = aae.lhs;
			if (target != null) {
				AtomDef<?> ad = toAtomDef(clazz, target);
				if (ad != null) {
					target = (Atom) ad.atom;
					cd = ad.classDef;
				} else {
					return new AtomDef<>(aae, cd);
				}
			}

			boolean mapClass = Map.class.isAssignableFrom(cd.clazz);
			if (List.class.isAssignableFrom(cd.clazz) || mapClass) {
				if (mapClass) {
					cd.typeParamIndex = 1;
				}
				return new AtomDef<>(new MethodInvocation(aae.getLocation(), target, "get", new Rvalue[] { copyRvalue(aae.index) }), cd);
			}

			return new AtomDef<>(aae, cd);
		}

		private AtomDef<Cast> toGetter(Class<?> clazz, Cast cast) throws CompileException {
			ClassDef cd = ClassDef.of(clazz);
			Class<?> targetClass = tryLoadClass(cast.targetType.toString());
			if (targetClass != null) {
				cd = ClassDef.of(targetClass);
			}
			return new AtomDef<>(cast, cd);
		}

		private AtomDef<Lvalue> toGetter(Class<?> clazz, FieldAccessExpression fae) throws CompileException {
			ClassDef cd = ClassDef.of(clazz);
			Atom target = fae.lhs;
			if (target != null) {
				AtomDef<?> ad = toAtomDef(clazz, target);
				if (ad != null) {
					target = (Atom) ad.atom;
					cd = ad.classDef;
				} else {
					return new AtomDef<>(fae, cd);
				}
			}

			if (cd.needsCast) {
				Class<?> typeArg = cd.getTypeArgument();
				if (typeArg != Object.class) {
					Cast cast = new Cast(fae.getLocation(), new ReferenceType(fae.getLocation(), new String[] { sourceClassName(typeArg) }, null), (Rvalue) target);
					target = new ParenthesizedExpression(fae.getLocation(), cast);
					cd = ClassDef.of(typeArg);
				}
			}

			try {
				Field field = cd.clazz.getField(fae.fieldName);
				return new AtomDef<>(new FieldAccessExpression(fae.getLocation(), target, fae.fieldName), ClassDef.of(field));
			} catch (NoSuchFieldException e) {
				// ignore, try getter
			}

			Method getter = findGetter(cd.clazz, fae.fieldName);

			if (getter != null) {
				MethodInvocation mi = new MethodInvocation(fae.getLocation(), target, getter.getName(), new Rvalue[0]);
				return new AtomDef<>(new ParenthesizedExpression(fae.getLocation(), mi), ClassDef.of(getter));
			} else {
				return new AtomDef<>(new FieldAccessExpression(fae.getLocation(), target, fae.fieldName), cd);
			}
		}

		public AtomDef<MethodInvocation> toGetter(Class<?> clazz, MethodInvocation mi) throws CompileException {
			ClassDef cd = ClassDef.of(clazz);
			Rvalue target = (Rvalue) mi.optionalTarget;
			if (target != null) {
				AtomDef<?> ad = toAtomDef(clazz, target);
				if (ad != null) {
					target = (Rvalue) ad.atom;
					cd = ad.classDef;
				} else {
					return new AtomDef<>(mi, cd);
				}

				setTypeParamIndex(cd, target instanceof MethodInvocation ? (MethodInvocation) target : mi);

				// workaround 2
				boolean miNot = findMethod(cd.clazz, mi.methodName, mi.arguments.length) == null;

				Class<?> typeArg = cd.getTypeArgument();
				Method meth = findMethod(typeArg, mi.methodName, mi.arguments.length);
				if (meth != null) {
					boolean needsCast = cd.needsCast;

					cd = ClassDef.of(meth);

					// try to infer from 1st argument
					if (!needsCast && meth.getGenericReturnType() instanceof ParameterizedType && mi.arguments.length > 0) {
						ClassDef argCd = resolveClass(lastClassDef.clazz, mi.arguments[0]);
						if (argCd != null) {
							cd = new ClassDef(meth.getReturnType(), argCd.clazz, true);
						}
					}

					if (needsCast && typeArg != Object.class && miNot) {
						Cast cast = new Cast(mi.getLocation(), new ReferenceType(mi.getLocation(), new String[] { sourceClassName(typeArg) }, null), target);
						target = new ParenthesizedExpression(mi.getLocation(), cast);
					}
				}
			} else if (mi.methodName.equals("$list") || mi.methodName.equals("$map") || mi.methodName.equals("$set")) {
				String methName = mi.methodName.substring(1);
				Method meth = findMethod(Util.class, methName, 0);
				int argIndex = mi.methodName.equals("$map") ? 1 : 0;
				// try to infer from argument index
				if (mi.arguments.length < argIndex + 1) {
					cd = ClassDef.of(meth);
				} else {
					ClassDef argCd = resolveClass(lastClassDef.clazz, mi.arguments[argIndex]);
					if (argCd != null) {
						cd = new ClassDef(meth.getReturnType(), argCd.clazz, true);
					}
				}
			} else {
				Method meth = findMethod(clazz, mi.methodName, mi.arguments.length);
				if (meth != null) {
					cd = ClassDef.of(meth);
				}
			}

			AtomDef<Rvalue[]> mad = copyMethodArgs(mi, cd);
			cd = mad.classDef;
			return new AtomDef<>(new MethodInvocation(mi.getLocation(), target, mi.methodName, mad.atom), cd);
		}

		private AtomDef<Rvalue[]> copyMethodArgs(MethodInvocation mi, ClassDef cd) throws CompileException {
			List<Rvalue> all = new ArrayList<>();

			Method meth = findMethod(cd.clazz, mi.methodName, mi.arguments.length);
			if (meth == null) {
				return new AtomDef<>(copyRvalues(mi.arguments), cd);
			}
			int i = 0;
			for (Rvalue a : mi.arguments) {
				if (a instanceof NewAnonymousClassInstance) {
					AtomDef<Rvalue> ad = convertLambdaAnon(meth, (NewAnonymousClassInstance) a, i, cd);
					cd = ad.classDef;
					all.add(ad.atom);
				} else {
					all.add(copyRvalue(a));
				}
				i++;
			}

			return new AtomDef<>(all.toArray(new Rvalue[all.size()]), cd);
		}

		private AtomDef<Rvalue> convertLambdaAnon(Method meth, NewAnonymousClassInstance arg, int argIndex, ClassDef cd) throws CompileException {
			Location loc = arg.getLocation();
			Class<?> pt = meth.getParameterTypes()[argIndex];
			String cc = sourceClassName(pt);
			String[] ids = cc.split("\\.");

			AnonymousClassDeclaration decl = new AnonymousClassDeclaration(loc, new ReferenceType(arg.getLocation(), ids, null));

			MethodDeclarator md = arg.anonymousClassDeclaration.getMethodDeclarations()
					.get(0);

			int nParams = md.formalParameters.parameters.length;
			Method lambdaMeth = findMethod(pt, null, nParams, true);
			if (lambdaMeth == null) {
				throw new NgoyException("Class %s does not have a method with parameter count %s", pt.getName(), nParams);
			}

			Class<?> rt = lambdaMeth.getReturnType();
			if (rt == void.class) {
				throw new NgoyException("Cannot convert lambda expression to a void method: %s.%s", pt.getName(), lambdaMeth.getName());
			}

			String rtc = sourceClassName(rt);
			String[] rtIds = rtc.split("\\.");

			FormalParameters mdParams = md.formalParameters;
			FormalParameter mdParam0 = mdParams.parameters[0];
			String _mdParam0 = format("_%s", mdParam0.name);
			FormalParameter p0 = new FormalParameter(loc, true, copyType(mdParams.parameters[0].type), _mdParam0);

			List<FormalParameter> paramsCopy = new ArrayList<>();
			paramsCopy.add(p0);
			for (FormalParameter pp : asList(mdParams.parameters).subList(1, mdParams.parameters.length)) {
				paramsCopy.add(copyFormalParameter(pp));
			}

			FormalParameters params = new FormalParameters(loc, paramsCopy.toArray(new FormalParameter[0]), false);

			String[] effectivePt = cd.getTypeArgument()
					.getName()
					.split("\\.");
			ReferenceType effectiveRefType = new ReferenceType(loc, effectivePt, null);
			List<BlockStatement> statements = new ArrayList<>();
			ArrayInitializerOrRvalue init = new Cast(loc, effectiveRefType, new AmbiguousName(loc, new String[] { _mdParam0 }));
			VariableDeclarator[] decls = new VariableDeclarator[] { new VariableDeclarator(loc, mdParam0.name, 0, init) };
			statements.add(new LocalVariableDeclarationStatement(loc, new Java.Modifiers(Mod.FINAL), effectiveRefType, decls));

			// cheap workaround
			if (meth.getName()
					.equals("map") && meth.getDeclaringClass() == Stream.class) {
				ClassDef returnCd = resolveClass(lastClassDef.clazz, ((ReturnStatement) md.optionalStatements.get(0)).optionalReturnValue);
				if (returnCd != null) {
					cd.genericType = returnCd.clazz;
				}
			}

			for (BlockStatement s : md.optionalStatements) {
				statements.add(copyBlockStatement(s));
			}

			decl.addDeclaredMethod(new MethodDeclarator(//
					loc, //
					null, //
					new Java.Modifiers(Mod.PUBLIC), //
					null, //
					new ReferenceType(loc, rtIds, null), //
					lambdaMeth.getName(), //
					params, //
					new org.codehaus.janino.Java.Type[0], //
					null, //
					statements //
			));
			return new AtomDef<>(new NewAnonymousClassInstance(loc, null, decl, new Rvalue[0]), cd);
		}

		private ClassDef resolveClass(Class<?> clazz, Rvalue atom) {

			atom = unwrapParenthesizedExpression(atom);

			if (atom instanceof MethodInvocation) {
				MethodInvocation mi = (MethodInvocation) atom;
				if (mi.optionalTarget == null) {
					Method meth = findMethod(clazz, mi.methodName, mi.arguments.length);
					return meth != null ? ClassDef.of(meth) : null;
				}

				ClassDef cd = resolveClass(clazz, (Rvalue) mi.optionalTarget);
				if (cd == null) {
					return null;
				}

				Method meth = findMethod(cd.clazz, mi.methodName, mi.arguments.length);
				if (meth != null) {
					return ClassDef.of(meth);
				}
			} else if (atom instanceof FieldAccessExpression) {
				FieldAccessExpression fae = (FieldAccessExpression) atom;
				ClassDef cd = resolveClass(clazz, (Rvalue) fae.lhs);
				if (cd == null) {
					return null;
				}
				try {
					return ClassDef.of(cd.clazz.getField(fae.fieldName));
				} catch (NoSuchFieldException e) {
					// ignore, try getter
				}

				Method getter = findGetter(cd.clazz, fae.fieldName);

				if (getter != null) {
					return ClassDef.of(getter);
				}

				return null;
			} else if (atom instanceof AmbiguousName) {
				return toGetter(clazz, (AmbiguousName) atom).classDef;
			} else if (atom instanceof StringLiteral) {
				return ClassDef.of(String.class);
			} else if (atom instanceof BooleanLiteral) {
				return ClassDef.of(Boolean.class);
			} else if (atom instanceof CharacterLiteral) {
				return ClassDef.of(Character.class);
			} else if (atom instanceof FloatingPointLiteral) {
				return ClassDef.of(Float.class);
			} else if (atom instanceof IntegerLiteral) {
				return ClassDef.of(Integer.class);
			} else if (atom instanceof NullLiteral) {
				return ClassDef.of(Object.class);
			} else if (atom instanceof NewClassInstance) {
				Class<?> cc = tryLoadClass(((NewClassInstance) atom).type.toString());
				if (cc != null) {
					return ClassDef.of(cc);
				}
			} else if (atom instanceof Cast) {
				Class<?> targetClass = tryLoadClass(((Cast) atom).targetType.toString());
				if (targetClass != null) {
					return ClassDef.of(targetClass);
				}
			} else if (atom instanceof BinaryOperation) {
				BinaryOperation bo = (BinaryOperation) atom;
				switch (bo.operator) {
				case ">":
				case "<":
				case ">=":
				case "<=":
				case "==":
				case "||":
				case "&&":
				case "!=":
					return ClassDef.of(boolean.class);
				case "|":
				case "&":
				case "^":
					return ClassDef.of(int.class);
				default:
					// depends on operands
					// TODO
				}
			}

			return null;
		}

		private AtomDef<?> toAtomDef(Class<?> clazz, Atom target) throws CompileException {
			AtomDef<?> ad = null;
			if (target instanceof MethodInvocation) {
				ad = toGetter(clazz, (MethodInvocation) target);
			} else if (target instanceof FieldAccessExpression) {
				ad = toGetter(clazz, (FieldAccessExpression) target);
			} else if (target instanceof AmbiguousName) {
				ad = toGetter(clazz, (AmbiguousName) target);
			} else if (target instanceof ArrayAccessExpression) {
				ad = toGetter(clazz, (ArrayAccessExpression) target);
			} else if (target instanceof Cast) {
				ad = toGetter(clazz, (Cast) target);
			} else if (target instanceof ParenthesizedExpression) {
				ad = toAtomDef(clazz, ((ParenthesizedExpression) target).value);
			}
			return ad;
		}

		private void setTypeParamIndex(ClassDef cd, MethodInvocation mi) {
			int typeParamIndex = 0;
			if (cd.needsCast) {
				Method meth = findMethod(cd.clazz, mi.methodName, mi.arguments.length);
				if (meth != null) {
					Type rt = meth.getGenericReturnType();
					String typeName = rt.getTypeName();
					if (rt instanceof ParameterizedType) {
						ParameterizedType pt = (ParameterizedType) rt;
						typeName = pt.getActualTypeArguments()[0].getTypeName();
					}

					TypeVariable<?>[] typeParameters = cd.clazz.getTypeParameters();
					for (int i = 0, n = typeParameters.length; i < n; i++) {
						if (typeParameters[i].getName()
								.equals(typeName)) {
							typeParamIndex = i;
							break;
						}
					}
				}
			}
			cd.typeParamIndex = typeParamIndex;
		}

		@Override
		public Lvalue copyAmbiguousName(AmbiguousName subject) throws CompileException {
			AtomDef<AmbiguousName> an = toGetter(lastClassDef.clazz, subject);
			lastClassDef = an.classDef;
			return an.atom;
		};

		@Override
		public Rvalue copyMethodInvocation(MethodInvocation subject) throws CompileException {
			AtomDef<MethodInvocation> mi = toGetter(lastClassDef.clazz, subject);
			lastClassDef = mi.classDef;

			ClassDef cd = lastClassDef;
			Rvalue target = mi.atom;
			if (cd.needsCast && cd.genericType instanceof ParameterizedType) {
				Class<?> typeArg = cd.getTypeArgument();
				if (typeArg != Object.class) {
					Method meth = findMethod(cd.clazz, subject.methodName, subject.arguments.length);
					if (meth != null) {
						target = new Cast(target.getLocation(), new ReferenceType(target.getLocation(), new String[] { sourceClassName(typeArg) }, null), target);
						lastClassDef = ClassDef.of(typeArg);
					}
				}
			}

			return target;
		}

		@Override
		public Rvalue copyCast(Cast subject) throws CompileException {
			AtomDef<Cast> mi = toGetter(lastClassDef.clazz, subject);
			lastClassDef = mi.classDef;
			return mi.atom;
		}

		@Override
		public Lvalue copyFieldAccessExpression(FieldAccessExpression subject) throws CompileException {
			AtomDef<Lvalue> fae = toGetter(lastClassDef.clazz, subject);
			lastClassDef = fae.classDef;
			return fae.atom;
		}

		@Override
		public Lvalue copyArrayAccessExpression(ArrayAccessExpression subject) throws CompileException {
			AtomDef<Rvalue> aae = toGetter(lastClassDef.clazz, subject);
			lastClassDef = aae.classDef;

			ClassDef cd = lastClassDef;
			Rvalue target = aae.atom;
			if (cd.needsCast && cd.genericType instanceof ParameterizedType) {
				Class<?> typeArg = cd.getTypeArgument();
				if (typeArg != Object.class) {
					target = new Cast(target.getLocation(), new ReferenceType(target.getLocation(), new String[] { sourceClassName(typeArg) }, null), target);
					lastClassDef = ClassDef.of(typeArg);
				}
			}

			return target instanceof Lvalue ? (Lvalue) target : new ParenthesizedExpression(target.getLocation(), target);
		}

		@Override
		public Rvalue copyNewInitializedArray(NewInitializedArray subject) throws CompileException {
			ArrayType arrayType = subject.arrayType;
			String type = Util.getArrayClass(arrayType.componentType.toString());
			Class<?> clazz = tryLoadClass(type);

			if (clazz != null) {
				lastClassDef = ClassDef.of(clazz);
			}

			return super.copyNewInitializedArray(subject);
		}

		@Override
		public Rvalue copyAssignment(Assignment subject) throws CompileException {
			throw new CompileException("Assignment is not allowed", subject.getLocation());
		}

		@Override
		public Rvalue copyCrement(Crement subject) throws CompileException {
			throw new CompileException("Increment/decrement is not allowed", subject.getLocation());
		}

		@Override
		public Rvalue copyThisReference(ThisReference subject) throws CompileException {
			throw new CompileException("Reference to 'this' is not allowed", subject.getLocation());
		}

		@Override
		public Rvalue copyQualifiedThisReference(QualifiedThisReference subject) throws CompileException {
			throw new CompileException("Reference to 'this' is not allowed", subject.getLocation());
		}

		@Override
		public Rvalue copyNewAnonymousClassInstance(NewAnonymousClassInstance subject) throws CompileException {
			if (subject.anonymousClassDeclaration.baseType.toString()
					.equals("LAMBDA")) {
				return super.copyNewAnonymousClassInstance(subject);
			}
			throw new CompileException("Anonymous class is not allowed", subject.getLocation());
		}

	}

	private FieldAccessToGetterParser() {
	}

	public interface CopyMore {
		Rvalue apply(Rvalue rvalue) throws CompileException;
	}

	public static String fieldAccessToGetter(Class<?> clazz, Map<String, Class<?>> prefixes, String expr, Map<String, Variable<?>> variables, CopyMore copyMore, @Nullable ClassDef[] outLastClassDef) {
		if (!isSet(expr)) {
			throw new NgoyException("Expression must not be empty");
		}

		expr = SmartStringParser.toJavaString(expr);
		expr = LambdaParser.parse(expr);

		try {
			Parser parser = new org.codehaus.janino.Parser(new Scanner(null, new StringReader(expr)));
			Rvalue[] rvalues = parser.parseExpressionList();
			if (rvalues.length != 1) {
				throw new NgoyException("The expression must be a single rvalue", expr);
			}

			if (copyMore == null) {
				copyMore = Objects::requireNonNull;
			}

			ToGetter toGetter = new ToGetter(clazz, prefixes, variables);
			Rvalue copy = copyMore.apply(unwrapParenthesizedExpression(toGetter.copyRvalue(rvalues[0])));

			StringWriter sw = new StringWriter();
			Unparser unparser = new Unparser(sw);
			unparser.unparseAtom(copy);
			unparser.close();

			if (outLastClassDef != null) {
				outLastClassDef[0] = toGetter.lastClassDef;
			}

			return sw.toString();
		} catch (Exception e) {
			throw new NgoyException("Error while compiling expression '%s': %s. The expression must be a single rvalue", expr,
					e instanceof CompileException ? getCompileExceptionMessageWithoutLocation((CompileException) e) : e.getMessage());
		}
	}
}
