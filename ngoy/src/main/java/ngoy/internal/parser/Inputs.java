package ngoy.internal.parser;

import static java.lang.String.format;
import static ngoy.core.NgoyException.wrap;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import jodd.jerry.Jerry;
import ngoy.core.Input;
import ngoy.core.NgoyException;

public class Inputs {

	private static final String SET_PREFIX = "set";

	public static String fieldName(String setter) {
		if (setter.startsWith(SET_PREFIX)) {
			String right = setter.substring(SET_PREFIX.length());
			if (right.isEmpty()) {
				return SET_PREFIX;
			}
			return Character.toLowerCase(right.charAt(0)) + right.substring(1);
		}
		return setter;
	}

	private Inputs() {
	}

	public interface InputCallback<T> {
		void run(T fieldOrMethod, Input input) throws Exception;
	}

	public static void withFieldInputs(Class<?> clazz, InputCallback<Field> cb) {
		try {
			for (Field field : clazz.getFields()) {
				Input input = field.getAnnotation(Input.class);
				if (input == null) {
					continue;
				}

				int mods = field.getModifiers();
				if (Modifier.isStatic(mods) || Modifier.isFinal(mods)) {
					throw new NgoyException("@Input annotated field must be public, non-final, non-static: %s.%s", clazz.getName(), field.getName());
				}
				cb.run(field, input);
			}
		} catch (Exception e) {
			throw wrap(e);
		}
	}

	public static void withMethodInputs(Class<?> clazz, InputCallback<Method> cb) {
		try {
			for (Method meth : clazz.getMethods()) {
				Input input = meth.getAnnotation(Input.class);
				if (input == null) {
					continue;
				}

				int mods = meth.getModifiers();
				if (Modifier.isStatic(mods)) {
					throw new NgoyException("@Input annotated method must be public, non-static: %s.%s", clazz.getName(), meth.getName());
				}

				if (meth.getParameterCount() != 1) {
					throw new ParseException("@Input annotated method must have exactly one parameter: %s.%s", clazz.getName(), meth.getName());
				}

				cb.run(meth, input);
			}
		} catch (Exception e) {
			throw wrap(e);
		}
	}

	public static List<CmpInput> cmpInputs(Jerry el, Class<?> clazz, Set<String> excludeBindings) {
		List<CmpInput> result = new ArrayList<>();
		withFieldInputs(clazz, (field, input) -> {
			String fieldName = field.getName();
			String binding = input.value();
			if (binding.isEmpty()) {
				binding = fieldName;
			}

			addInput(InputType.FIELD, ValueType.TEXT, result, el, binding, fieldName, excludeBindings);
			addInput(InputType.FIELD, ValueType.EXPR, result, el, binding, fieldName, excludeBindings);
		});

		withMethodInputs(clazz, (meth, input) -> {
			String methodName = meth.getName();
			String binding = input.value();
			if (binding.isEmpty()) {
				binding = fieldName(methodName);
			}

			addInput(InputType.METHOD, ValueType.TEXT, result, el, binding, methodName, excludeBindings);
			addInput(InputType.METHOD, ValueType.EXPR, result, el, binding, methodName, excludeBindings);
		});
		return result;
	}

	public static enum InputType {
		FIELD, METHOD;
	}

	public static enum ValueType {
		EXPR, TEXT;
	}

	public static class CmpInput {
		public final InputType type;
		public final String input;
		public final ValueType valueType;
		public final String value;

		public CmpInput(InputType type, String input, ValueType valueType, String value) {
			this.type = type;
			this.input = input;
			this.valueType = valueType;
			this.value = value;
		}
	}

	private static void addInput(InputType inputType, ValueType valueType, List<CmpInput> result, Jerry el, String input, String fieldName, Set<String> excludeBindings) {
		boolean isExpr = valueType == ValueType.EXPR;
		String attr = isExpr ? format("[%s]", input) : input;
		String inp = el.attr(attr);
		if (inp == null) {
			return;
		}

		result.add(new CmpInput(inputType, fieldName, valueType, inp));

		excludeBindings.add(input.toLowerCase());
		excludeBindings.add(fieldName.toLowerCase());
	}

}
