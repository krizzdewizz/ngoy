package ngoy.internal.parser;

import static java.lang.String.format;

import java.io.StringReader;
import java.io.StringWriter;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Map;

import org.codehaus.janino.Java.AmbiguousName;
import org.codehaus.janino.Java.Atom;
import org.codehaus.janino.Java.Rvalue;
import org.codehaus.janino.Parser;
import org.codehaus.janino.Scanner;
import org.codehaus.janino.Unparser;
import org.codehaus.janino.util.AbstractTraverser;

import ngoy.core.NgoyException;
import ngoy.core.Util;

/**
 * Converts a field access to a getter call.
 * <p>
 * experimental.
 * 
 * @author krizz
 */
public final class FieldAccessToGetterParser {

	private static class ToGetter extends AbstractTraverser<RuntimeException> {

		private static final Field MODIFIERS_FIELD;
		private static final Field IDENTIFIERS_FIELD;

		static {
			try {
				IDENTIFIERS_FIELD = AmbiguousName.class.getField("identifiers");

				MODIFIERS_FIELD = Field.class.getDeclaredField("modifiers");
				MODIFIERS_FIELD.setAccessible(true);

				MODIFIERS_FIELD.setInt(IDENTIFIERS_FIELD, IDENTIFIERS_FIELD.getModifiers() & ~Modifier.FINAL);
			} catch (Exception e) {
				throw NgoyException.wrap(e);
			}
		}

		private final Class<?> clazz;
		private final Map<String, Class<?>> prefixes;

		public ToGetter(Class<?> clazz, Map<String, Class<?>> prefixes) {
			this.clazz = clazz;
			this.prefixes = prefixes;
		}

		private void toGetter(AmbiguousName ambiguousName) {
			String[] ids = ambiguousName.identifiers;

			Class<?> prefix = prefixes.get(ids[0]);

			Class<?> c;
			int startIndex;
			if (prefix != null) {
				c = prefix;
				startIndex = 1;
			} else {
				c = clazz;
				startIndex = 0;
			}

			String[] idsCopy = new String[ids.length];
			System.arraycopy(ids, 0, idsCopy, 0, ids.length);
			boolean changed = false;

			for (int i = startIndex, n = ids.length; i < n; i++) {
				String id = ids[i];
				String getterName = Util.toGetter(id);
				Method getter = null;
				try {
					getter = c.getMethod(getterName);
				} catch (NoSuchMethodException e) {
					// ignore
				} catch (SecurityException e) {
					throw NgoyException.wrap(e);
				}

				if (getter != null) {
					idsCopy[i] = format("%s()", getterName);
					changed = true;
				}

				if (i + 1 >= ids.length) {
					break;
				}

				Class<?> propClass = null;
				if (getter != null) {
					propClass = getter.getReturnType();
				} else {
					try {
						propClass = c.getField(id)
								.getType();
					} catch (NoSuchFieldException e) {
						// ignore
					} catch (SecurityException e) {
						throw NgoyException.wrap(e);
					}
				}

				if (propClass == null) {
					break; // give up
				}

				c = propClass;
			}

			if (changed) {
				try {
					IDENTIFIERS_FIELD.set(ambiguousName, idsCopy);
				} catch (Exception e) {
					throw NgoyException.wrap(e);
				}
			}
		}

		public void traverseRvalue(Rvalue rv) {
			if (rv instanceof AmbiguousName) {
				toGetter((AmbiguousName) rv);
			}
		}
	}

	private FieldAccessToGetterParser() {
	}

	public static String fieldAccessToGetter(Class<?> clazz, Map<String, Class<?>> prefixes, String expr) {
		try {
			Parser parser = new org.codehaus.janino.Parser(new Scanner(null, new StringReader(expr)));
			Atom atom = parser.parseExpression();

			new ToGetter(clazz, prefixes).visitAtom(atom);

			StringWriter sw = new StringWriter();
			Unparser unparser = new Unparser(sw);
			unparser.unparseAtom(atom);
			unparser.close();

			return sw.toString();
		} catch (Exception e) {
			throw NgoyException.wrap(e);
		}

	}
}
