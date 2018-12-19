package ngoy.internal.parser;

import static java.lang.String.format;
import static ngoy.core.NgoyException.wrap;
import static ngoy.core.Util.getCompileExceptionMessageWithoutLocation;
import static ngoy.core.Util.isSet;

import java.io.StringReader;
import java.io.StringWriter;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.Map;
import java.util.stream.Stream;

import org.codehaus.commons.compiler.CompileException;
import org.codehaus.janino.Java.AmbiguousName;
import org.codehaus.janino.Java.Assignment;
import org.codehaus.janino.Java.Atom;
import org.codehaus.janino.Java.Cast;
import org.codehaus.janino.Java.Crement;
import org.codehaus.janino.Java.FieldAccessExpression;
import org.codehaus.janino.Java.Lvalue;
import org.codehaus.janino.Java.MethodInvocation;
import org.codehaus.janino.Java.NewAnonymousClassInstance;
import org.codehaus.janino.Java.ParenthesizedExpression;
import org.codehaus.janino.Java.QualifiedThisReference;
import org.codehaus.janino.Java.ReferenceType;
import org.codehaus.janino.Java.Rvalue;
import org.codehaus.janino.Java.ThisReference;
import org.codehaus.janino.Parser;
import org.codehaus.janino.Scanner;
import org.codehaus.janino.Unparser;
import org.codehaus.janino.util.DeepCopier;

import ngoy.core.NgoyException;
import ngoy.core.Nullable;
import ngoy.core.Variable;

/**
 * Converts a field access to a getter call.
 * <p>
 * experimental.
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

	@Nullable
	private static Method findMethod(Class<?> c, String methodName, int nArgs) {
		return Stream.of(c.getMethods())
				.filter(meth -> {
					int mods = meth.getModifiers();
					return meth.getName()
							.equals(methodName) && meth.getParameterCount() == nArgs && Modifier.isPublic(mods) && !Modifier.isStatic(mods);
				})
				.findFirst()
				.orElse(null);
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

			Class<?> prefix = prefixes.get(ids[0]);

			ClassDef[] cd = new ClassDef[1];
			int startIndex;
			if (prefix != null) {
				cd[0] = ClassDef.of(prefix);
				startIndex = 1;
			} else {
				cd[0] = ClassDef.of(clazz);
				startIndex = 0;
			}

			String[] idsCopy = new String[ids.length];
			System.arraycopy(ids, 0, idsCopy, 0, ids.length);

			for (int i = startIndex, n = ambiguousName.n; i < n; i++) {
				String id = ids[i];

				try {
					cd[0] = ClassDef.of(cd[0].clazz.getField(id));
					continue;
				} catch (NoSuchFieldException e) {
					// ignore, try getter
				} catch (SecurityException e) {
					throw wrap(e);
				}

				Method getter = findGetter(cd[0].clazz, id);

				if (getter != null) {
					idsCopy[i] = format("%s()", getter.getName());
					cd[0] = ClassDef.of(getter);
				} else {
					Variable<?> variable = variables.get(id);
					if (variable != null) {
						cd[0] = ClassDef.of(variable.type);
					}
				}
			}

			return new AtomDef<>(new AmbiguousName(ambiguousName.getLocation(), idsCopy, ambiguousName.n), cd[0]);
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

			Atom lhs = fae.lhs;
			if (cd.needsCast) {
				Class<?> typeArg = cd.getTypeArgument();
				if (typeArg != Object.class) {
					Cast cast = new Cast(fae.getLocation(), new ReferenceType(fae.getLocation(), new String[] { typeArg.getName() }, null), (Rvalue) fae.lhs);
					lhs = new ParenthesizedExpression(fae.getLocation(), cast);
					cd = ClassDef.of(typeArg);
				}
			}

			try {
				Field field = cd.clazz.getField(fae.fieldName);
				return new AtomDef<>(new FieldAccessExpression(fae.getLocation(), lhs, fae.fieldName), ClassDef.of(field));
			} catch (NoSuchFieldException e) {
				// ignore, try getter
			}

			Method getter = findGetter(cd.clazz, fae.fieldName);

			if (getter != null) {
				MethodInvocation mi = new MethodInvocation(fae.getLocation(), lhs, getter.getName(), new Rvalue[0]);
				return new AtomDef<>(new ParenthesizedExpression(fae.getLocation(), mi), ClassDef.of(getter));
			} else {
				return new AtomDef<>(new FieldAccessExpression(fae.getLocation(), lhs, fae.fieldName), cd);
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

				Class<?> typeArg = cd.getTypeArgument();
				Method meth = findMethod(typeArg, mi.methodName, mi.arguments.length);
				if (meth != null) {
					if (cd.needsCast && typeArg != Object.class) {
						Cast cast = new Cast(mi.getLocation(), new ReferenceType(mi.getLocation(), new String[] { typeArg.getName() }, null), target);
						target = new ParenthesizedExpression(mi.getLocation(), cast);
					}
					cd = ClassDef.of(meth);
				}
			} else {
				Method meth = findMethod(clazz, mi.methodName, mi.arguments.length);
				if (meth != null) {
					cd = ClassDef.of(meth);
				}
			}

			return new AtomDef<>(new MethodInvocation(mi.getLocation(), target, mi.methodName, copyRvalues(mi.arguments)), cd);
		}

		private AtomDef<?> toAtomDef(Class<?> clazz, Atom target) throws CompileException {
			AtomDef<?> ad = null;
			if (target instanceof MethodInvocation) {
				ad = toGetter(clazz, (MethodInvocation) target);
			} else if (target instanceof FieldAccessExpression) {
				ad = toGetter(clazz, (FieldAccessExpression) target);
			} else if (target instanceof AmbiguousName) {
				ad = toGetter(clazz, (AmbiguousName) target);
			}
			return ad;
		}

		private void setTypeParamIndex(ClassDef cd, MethodInvocation mi) {
			int typeParamIndex = 0;
			if (cd.needsCast) {
				Method meth = findMethod(cd.clazz, mi.methodName, mi.arguments.length);
				if (meth != null) {
					Type tt = meth.getGenericReturnType();
					String typeName = tt.getTypeName();
					if (tt instanceof ParameterizedType) {
						ParameterizedType pt = (ParameterizedType) tt;
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

		public Lvalue copyAmbiguousName(AmbiguousName subject) throws CompileException {
			AtomDef<AmbiguousName> an = toGetter(lastClassDef.clazz, subject);
			lastClassDef = an.classDef;
			return an.atom;
		};

		@Override
		public Rvalue copyMethodInvocation(MethodInvocation subject) throws CompileException {
			AtomDef<MethodInvocation> mi = toGetter(lastClassDef.clazz, subject);
			lastClassDef = mi.classDef;
			return mi.atom;
		}

		@Override
		public Lvalue copyFieldAccessExpression(FieldAccessExpression subject) throws CompileException {
			AtomDef<Lvalue> fae = toGetter(lastClassDef.clazz, subject);
			lastClassDef = fae.classDef;
			return fae.atom;
		}
	}

	private FieldAccessToGetterParser() {
	}

	public static String fieldAccessToGetter(Class<?> clazz, Map<String, Class<?>> prefixes, String expr, Map<String, Variable<?>> variables, @Nullable ClassDef[] outLastClassDef) {
		if (!isSet(expr)) {
			throw new NgoyException("Expression must not be empty");
		}

		try {
			Parser parser = new org.codehaus.janino.Parser(new Scanner(null, new StringReader(expr)));
			Rvalue rvalue;
			try {
				Rvalue[] rvalues = parser.parseExpressionList();
				if (rvalues.length != 1) {
					throw new NgoyException("Error while compiling expression '%s'. The expression must be a single rvalue.", expr);
				}
				rvalue = rvalues[0];

				if (rvalue instanceof Assignment) {
					throw new NgoyException("Error while compiling expression '%s'. Assignment is not allowed.", expr);
				} else if (rvalue instanceof Crement) {
					throw new NgoyException("Error while compiling expression '%s'. Increment/decrement is not allowed.", expr);
				} else if (rvalue instanceof ThisReference || rvalue instanceof QualifiedThisReference) {
					throw new NgoyException("Error while compiling expression '%s'. Reference to 'this' is not allowed.", expr);
				} else if (rvalue instanceof NewAnonymousClassInstance) {
					throw new NgoyException("Error while compiling expression '%s'. Anonymous class is not allowed.", expr);
				}

			} catch (CompileException e) {
				throw new NgoyException("Error while compiling expression '%s': %s. The expression must be a single rvalue.", expr, getCompileExceptionMessageWithoutLocation(e));
			}

			ToGetter toGetter = new ToGetter(clazz, prefixes, variables);

			Rvalue copyAtom = unwrapParenthesizedExpression(toGetter.copyRvalue(rvalue));

			StringWriter sw = new StringWriter();
			Unparser unparser = new Unparser(sw);
			unparser.unparseAtom(copyAtom);
			unparser.close();

			if (outLastClassDef != null) {
				outLastClassDef[0] = toGetter.lastClassDef;
			}

			return sw.toString();
		} catch (Exception e) {
			throw wrap(e);
		}
	}
}
