package ngoy.internal.parser;

import static java.lang.String.format;
import static ngoy.core.NgoyException.wrap;

import java.io.StringReader;
import java.io.StringWriter;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Map;
import java.util.stream.Stream;

import org.codehaus.janino.Java.AmbiguousName;
import org.codehaus.janino.Java.Atom;
import org.codehaus.janino.Java.FieldAccessExpression;
import org.codehaus.janino.Java.MethodInvocation;
import org.codehaus.janino.Java.Rvalue;
import org.codehaus.janino.Parser;
import org.codehaus.janino.Scanner;
import org.codehaus.janino.Unparser;
import org.codehaus.janino.util.AbstractTraverser;

import ngoy.core.NgoyException;
import ngoy.core.Nullable;
import ngoy.core.Util;
import ngoy.core.Variable;

/**
 * Converts a field access to a getter call.
 * <p>
 * experimental.
 * 
 * @author krizz
 */
public final class FieldAccessToGetterParser {

	public static class ListItemDef {
		public final Class<?> clazz;
		public final String typeName;

		public ListItemDef(Class<?> clazz, String typeName) {
			this.clazz = clazz;
			this.typeName = typeName;
		}
	}

	public static class ClassDef {
		public final Class<?> clazz;
		public final Type genericType;

		public ClassDef(Class<?> clazz, Type genericType) {
			this.clazz = clazz;
			this.genericType = genericType;
		}

		public boolean valid() {
			return clazz.isArray() || Iterable.class.isAssignableFrom(clazz);
		}

		private String fixGenericTypeName(String itemType) {
			// see GenericTypeWrongTest
			return itemType.replace("java.util.Map.java.util.Map", "java.util.Map");
		}

		public ListItemDef getListItemType(ClassDef classDef) {
			Class<?> itemType = Object.class;
			String itemTypeName = "Object";
			if (classDef.clazz.isArray()) {
				itemType = classDef.clazz.getComponentType();
				itemTypeName = itemType.getName();
			} else if (Collection.class.isAssignableFrom(classDef.clazz)) {
				if (classDef.genericType instanceof ParameterizedType) {
					String pt = ((ParameterizedType) classDef.genericType).getActualTypeArguments()[0].getTypeName();

					pt = fixGenericTypeName(pt);

					itemTypeName = pt;

					int pos = pt.indexOf('<');
					if (pos >= 0) {
						pt = pt.substring(0, pos);
					}

					try {
						itemType = getClass().getClassLoader()
								.loadClass(pt);
					} catch (ClassNotFoundException e) {
						throw NgoyException.wrap(e);
					}
				}
			}
			return new ListItemDef(itemType, itemTypeName);
		}
	}

	private static class ToGetter extends AbstractTraverser<RuntimeException> {

		private static final Field MODIFIERS_FIELD;
		private static final Field IDENTIFIERS_FIELD;
		private static final Field FIELD_NAME_FIELD;

		static {
			try {
				IDENTIFIERS_FIELD = AmbiguousName.class.getField("identifiers");
				FIELD_NAME_FIELD = FieldAccessExpression.class.getField("fieldName");

				MODIFIERS_FIELD = Field.class.getDeclaredField("modifiers");
				MODIFIERS_FIELD.setAccessible(true);

				MODIFIERS_FIELD.setInt(IDENTIFIERS_FIELD, IDENTIFIERS_FIELD.getModifiers() & ~Modifier.FINAL);
				MODIFIERS_FIELD.setInt(FIELD_NAME_FIELD, FIELD_NAME_FIELD.getModifiers() & ~Modifier.FINAL);
			} catch (Exception e) {
				throw wrap(e);
			}
		}

		private final Class<?> clazz;
		private final Map<String, Class<?>> prefixes;
		private final Map<String, Variable<?>> variables;
		private ClassDef[] outLastClassDef;

		public ToGetter(Class<?> clazz, Map<String, Class<?>> prefixes, Map<String, Variable<?>> variables, ClassDef[] outLastClassDef) {
			this.variables = variables;
			if (outLastClassDef == null) {
				outLastClassDef = new ClassDef[1];
			}
			this.clazz = clazz;
			this.prefixes = prefixes;
			outLastClassDef[0] = new ClassDef(clazz, null);
			this.outLastClassDef = outLastClassDef;
		}

		private void toGetter(AmbiguousName ambiguousName) {
			String[] ids = ambiguousName.identifiers;

			Class<?> prefix = prefixes.get(ids[0]);

			Class<?>[] c = new Class[1];
			Type[] genericType = new Type[1];
			int startIndex;
			if (prefix != null) {
				c[0] = prefix;
				startIndex = 1;
			} else {
				c[0] = clazz;
				startIndex = 0;
			}

			String[] idsCopy = new String[ids.length];
			System.arraycopy(ids, 0, idsCopy, 0, ids.length);
			boolean changed = false;

			for (int i = startIndex, n = ids.length; i < n; i++) {
				String id = ids[i];

				try {
					Field field = c[0].getField(id);
					genericType[0] = field.getGenericType();
					c[0] = field.getType();
					continue;
				} catch (NoSuchFieldException e) {
					// ignore
				} catch (SecurityException e) {
					throw wrap(e);
				}

				Class<?>[] nextClass = new Class[1];

				Method getter = Util.toGetter(id, name -> {
					try {
						return c[0].getMethod(name);
					} catch (NoSuchMethodException e) {
						// ignore
						return null;
					} catch (SecurityException e) {
						throw wrap(e);
					}
				});

				if (getter != null) {
					idsCopy[i] = format("%s()", getter.getName());
					nextClass[0] = getter.getReturnType();
					genericType[0] = getter.getGenericReturnType();
					changed = true;
				} else {
					Variable<?> variable = variables.get(id);
					if (variable != null) {
						nextClass[0] = variable.type;
						genericType[0] = null;
					}
				}

				if (nextClass[0] != null) {
					c[0] = nextClass[0];
				}

				if (nextClass[0] == null || i + 1 >= ids.length) {
					break;
				}
			}

			outLastClassDef[0] = new ClassDef(c[0], genericType[0]);

			if (changed) {
				try {
					IDENTIFIERS_FIELD.set(ambiguousName, idsCopy);
				} catch (Exception e) {
					throw wrap(e);
				}
			}
		}

		public void traverseRvalue(Rvalue rv) {
			if (rv instanceof AmbiguousName) {
				toGetter((AmbiguousName) rv);
			} else if (rv instanceof MethodInvocation) {
				MethodInvocation mi = (MethodInvocation) rv;
				Method rt = findMethod(outLastClassDef[0].clazz, mi.methodName, mi.arguments.length);
				if (rt != null) {
					outLastClassDef[0] = new ClassDef(rt.getReturnType(), rt.getGenericReturnType());
				}
			} else if (rv instanceof FieldAccessExpression) {
				FieldAccessExpression fae = (FieldAccessExpression) rv;

				Method getter = Util.toGetter(fae.fieldName, name -> {
					try {
						return outLastClassDef[0].clazz.getMethod(name);
					} catch (NoSuchMethodException e) {
						// ignore
						return null;
					} catch (SecurityException e) {
						throw wrap(e);
					}
				});

				if (getter != null) {
					outLastClassDef[0] = new ClassDef(getter.getReturnType(), getter.getGenericReturnType());
					try {
						FIELD_NAME_FIELD.set(fae, format("%s()", getter.getName()));
					} catch (Exception e) {
						throw wrap(e);
					}
				}
			}
		}

		@Nullable
		private Method findMethod(Class<?> c, String methodName, int nArgs) {
			return Stream.of(c.getMethods())
					.filter(meth -> {
						int mods = meth.getModifiers();
						return meth.getName()
								.equals(methodName) && meth.getParameterCount() == nArgs && Modifier.isPublic(mods) && !Modifier.isStatic(mods);
					})
					.findFirst()
					.orElse(null);
		}

	}

	private FieldAccessToGetterParser() {
	}

	public static String fieldAccessToGetter(Class<?> clazz, Map<String, Class<?>> prefixes, String expr, Map<String, Variable<?>> variables, ClassDef[] outLastClassDef) {
		try {
			Parser parser = new org.codehaus.janino.Parser(new Scanner(null, new StringReader(expr)));
			Atom atom = parser.parseExpression();

			new ToGetter(clazz, prefixes, variables, outLastClassDef).visitAtom(atom);

			StringWriter sw = new StringWriter();
			Unparser unparser = new Unparser(sw);
			unparser.unparseAtom(atom);
			unparser.close();

			return sw.toString();
		} catch (Exception e) {
			throw wrap(e);
		}
	}
}
