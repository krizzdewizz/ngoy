package org.ngoy.internal.parser;

import static java.lang.String.format;
import static org.ngoy.core.Util.fieldName;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.jsoup.nodes.Element;
import org.ngoy.core.ElementRef;
import org.ngoy.core.Input;
import org.ngoy.core.internal.JSoupElementRef;

public class Inputs {

	private Inputs() {
	}

	public static final char FIELD_INPUT = '0';
	public static final char METHOD_INPUT = '1';
	public static final char VALUE_EXPR = '0';
	public static final char VALUE_TEXT = '1';

	public static List<String> cmpInputs(ElementRef elRef, Class<?> clazz, Set<String> excludeBindings) {

		Element el = ((JSoupElementRef) elRef).getNativeElement();

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

			addInput(FIELD_INPUT, VALUE_TEXT, result, el, fieldName, binding, excludeBindings);
			addInput(FIELD_INPUT, VALUE_EXPR, result, el, fieldName, binding, excludeBindings);
		}

		for (Method m : clazz.getMethods()) {
			Input input = m.getAnnotation(Input.class);
			if (input == null) {
				continue;
			}

			if (m.getParameterCount() != 1) {
				throw new ParseException("input setter method %s.%s must have exactly 1 parameter.", clazz.getName(), m.getName());
			}

			String methodName = m.getName();
			String binding = input.value();
			if (binding.isEmpty()) {
				binding = fieldName(methodName);
			}

			addInput(METHOD_INPUT, VALUE_TEXT, result, el, methodName, binding, excludeBindings);
			addInput(METHOD_INPUT, VALUE_EXPR, result, el, methodName, binding, excludeBindings);
		}
		return result;
	}

	private static void addInput(char inputType, char valueType, List<String> result, Element el, String fieldName, String binding, Set<String> excludeBindings) {
		String attr = valueType == VALUE_EXPR ? format("[%s]", binding) : binding;
		String inp = el.attr(attr);
		if (!inp.isEmpty()) {
			result.add(format("%s%s%s", inputType, valueType, fieldName));
			result.add(inp);
			excludeBindings.add(fieldName);
			excludeBindings.add(binding);
		}
	}

}
