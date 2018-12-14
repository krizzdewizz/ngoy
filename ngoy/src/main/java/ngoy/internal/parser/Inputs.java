package ngoy.internal.parser;

import static java.lang.String.format;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import jodd.jerry.Jerry;
import ngoy.core.Input;
import ngoy.core.NgoyException;
import ngoy.core.internal.Resolver;

public class Inputs {

	private static final String SET_PREFIX = "set";

	static String fieldName(String setter) {
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

	public static List<CmpInput> cmpInputs(Jerry el, Class<?> clazz, Set<String> excludeBindings, Resolver resolver) {
		List<CmpInput> result = new ArrayList<>();
		for (Field f : clazz.getFields()) {
			Input input = f.getAnnotation(Input.class);
			if (input == null) {
				continue;
			}

			String fieldName = f.getName();
			String binding = input.value();
			if (binding.isEmpty()) {
				binding = fieldName;
			}

			addInput(InputType.FIELD, ValueType.TEXT, result, el, binding, fieldName, f.getType(), clazz, excludeBindings, resolver);
			addInput(InputType.FIELD, ValueType.EXPR, result, el, binding, fieldName, f.getType(), clazz, excludeBindings, resolver);
		}

		for (Method m : clazz.getMethods()) {
			Input input = m.getAnnotation(Input.class);
			if (input == null) {
				continue;
			}

			if (m.getParameterCount() != 1) {
				throw new ParseException("input setter method %s.%s must have exactly one parameter.", clazz.getName(), m.getName());
			}

			String methodName = m.getName();
			String binding = input.value();
			if (binding.isEmpty()) {
				binding = fieldName(methodName);
			}

			Class<?> paramType = m.getParameters()[0].getType();
			addInput(InputType.METHOD, ValueType.TEXT, result, el, binding, methodName, paramType, clazz, excludeBindings, resolver);
			addInput(InputType.METHOD, ValueType.EXPR, result, el, binding, methodName, paramType, clazz, excludeBindings, resolver);
		}
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
		public final Class<?> inputClass;

		public CmpInput(InputType type, String input, Class<?> inputClass, ValueType valueType, String value) {
			this.type = type;
			this.input = input;
			this.inputClass = inputClass;
			this.valueType = valueType;
			this.value = value;
		}
	}

	private static void addInput(InputType inputType, ValueType valueType, List<CmpInput> result, Jerry el, String input, String fieldName, Class<?> fieldType, Class<?> clazz,
			Set<String> excludeBindings, Resolver resolver) {
		boolean isExpr = valueType == ValueType.EXPR;
		String attr = isExpr ? format("[%s]", input) : input;
		String inp = el.attr(attr);
		if (inp == null) {
			return;
		}

		if (!isExpr && !fieldType.isAssignableFrom(String.class)) {
			throw new NgoyException("The input '%s' on component %s expects value of type %s but would receive a string. Use a binding expression %s instead.", input, clazz.getName(),
					fieldType.getName(), format("[%s]", input));
		}

		result.add(new CmpInput(inputType, fieldName, fieldType, valueType, isExpr ? ExprParser.convertPipesToTransformCalls(inp, resolver) : inp));

		excludeBindings.add(input.toLowerCase());
		excludeBindings.add(fieldName.toLowerCase());
	}

}
