package ngoy.internal.parser;

import static ngoy.core.Util.isSet;

import java.util.Map;

import ngoy.core.NgoyException;
import ngoy.core.Nullable;
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
 * 
 * @author krizz
 */
public final class FieldAccessToGetterParser {

	private FieldAccessToGetterParser() {
	}

	public static String fieldAccessToGetter(Class<?> clazz, Map<String, Class<?>> prefixes, String expr, Map<String, Variable<?>> variables, @Nullable ClassDef[] outLastClassDef) {

		if (!isSet(expr)) {
			throw new NgoyException("Expression must not be empty");
		}

		expr = SmartStringParser.toJavaString(expr);

		return expr;
	}
}
