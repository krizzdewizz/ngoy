package org.ngoy.internal.parser;

import static java.lang.String.format;
import static org.ngoy.core.Util.fieldName;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.jsoup.nodes.Element;
import org.ngoy.core.Input;
import org.ngoy.core.NgoyException;

public class Inputs {

	private Inputs() {
	}

	public static final char FIELD_INPUT = '0';
	public static final char METHOD_INPUT = '1';
	public static final char VALUE_EXPR = '0';
	public static final char VALUE_TEXT = '1';

	public static List<String> cmpInputs(Element el, Class<?> clazz, Set<String> excludeBindings) {
		List<String> result = new ArrayList<>();
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

			addInput(FIELD_INPUT, VALUE_TEXT, result, el, binding, fieldName, f.getType(), clazz, excludeBindings);
			addInput(FIELD_INPUT, VALUE_EXPR, result, el, binding, fieldName, f.getType(), clazz, excludeBindings);
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

			addInput(METHOD_INPUT, VALUE_TEXT, result, el, binding, methodName, m.getReturnType(), clazz, excludeBindings);
			addInput(METHOD_INPUT, VALUE_EXPR, result, el, binding, methodName, m.getReturnType(), clazz, excludeBindings);
		}
		return result;
	}

	private static void addInput(char inputType, char valueType, List<String> result, Element el, String input, String fieldName, Class<?> fieldType, Class<?> clazz, Set<String> excludeBindings) {
		String attr = valueType == VALUE_EXPR ? format("[%s]", input) : input;
		String inp = el.attr(attr);
		if (!inp.isEmpty()) {

			if (valueType == VALUE_TEXT && !fieldType.equals(String.class)) {
				throw new NgoyException("The input '%s' on component %s expects value of type %s but would receive a string. Use a binding expression %s instead.", input, clazz.getName(),
						fieldType.getName(), format("[%s]", input));
			}

			result.add(format("%s%s%s", inputType, valueType, fieldName));
			result.add(inp);
			excludeBindings.add(input.toLowerCase());
			excludeBindings.add(fieldName.toLowerCase());
		}
	}

}
