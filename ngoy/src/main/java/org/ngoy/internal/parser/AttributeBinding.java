package org.ngoy.internal.parser;

import static java.lang.String.format;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Set;

import org.jsoup.nodes.Attribute;
import org.jsoup.nodes.Element;
import org.ngoy.core.HostBinding;

public class AttributeBinding {

	private static final String BINDING_CLASS = "class.";
	private static final String BINDING_ATTR = "attr.";
	private static final String BINDING_TEXT = "text";

	static void addAttributeBinding(ParserHandler handler, String name, String value, Set<String> exclude, List<String[]> targetClassNames, List<String[]> targetAttrNames) {
		if (!name.endsWith("]")) {
			throw new ParseException("Attribute binding malformed: missing ]");
		}
		String rawName = name.substring(1, name.length() - 1);

		if (exclude.contains(rawName.toLowerCase())) {
			return;
		}

		if (rawName.equals("class")) {
			ObjParser.parse(value)
					.forEach((key, expr) -> targetClassNames.add(new String[] { key, expr }));
		} else if (rawName.startsWith(BINDING_CLASS)) {
			String className = rawName.substring(BINDING_CLASS.length());
			targetClassNames.add(new String[] { className, value });
		} else if (rawName.startsWith(BINDING_ATTR)) {
			String attrName = rawName.substring(BINDING_ATTR.length());
			targetAttrNames.add(new String[] { attrName, value });
		} else if (rawName.equals(BINDING_TEXT)) {
			handler.textOverride(value);
		} else {
			handler.attributeExpr(rawName, value);
		}
	}

	static void replaceAttrs(Parser parser, Element el, Set<String> excludeBindings, List<String[]> targetClassNames, List<String[]> targetAttrNames) {
		for (Attribute attr : el.attributes()) {
			String name = attr.getKey();
			if (name.equals("class") || name.startsWith("*")) {
				continue;
			}

			if (name.startsWith("[")) {
				addAttributeBinding(parser.handler, name, attr.getValue(), excludeBindings, targetClassNames, targetAttrNames);
			} else if (!excludeBindings.contains(name)) {
				boolean hasValue = attr.getValue() != null;
				parser.handler.attributeStart(name, hasValue);
				if (hasValue) {
					parser.replaceExpr(attr.getValue());
					parser.handler.attributeEnd();
				}
			}
		}
	}

	static void addHostAttributeBindings(Parser parser, Class<?> cmpClass, Set<String> excludeBindings, List<String[]> classNames, List<String[]> attrNames) {
		for (Field f : cmpClass.getFields()) {
			HostBinding hb = f.getAnnotation(HostBinding.class);
			if (hb == null) {
				continue;
			}

			addAttributeBinding(parser.handler, format("[%s]", hb.value()), f.getName(), excludeBindings, classNames, attrNames);
		}

		for (Method m : cmpClass.getMethods()) {
			HostBinding hb = m.getAnnotation(HostBinding.class);
			if (hb == null) {
				continue;
			}

			if (m.getParameterCount() > 0) {
				throw new ParseException("Host binding method must not have parameters: %s.%s", cmpClass.getName(), m.getName());
			}

			addAttributeBinding(parser.handler, format("[%s]", hb.value()), format("%s()", m.getName()), excludeBindings, classNames, attrNames);
		}
	}

	static void replaceAttrExpr(Parser parser, List<String[]> classNames, List<String[]> attrNames) {
		if (!classNames.isEmpty()) {
			parser.handler.attributeClasses(classNames);
		}

		if (!attrNames.isEmpty()) {
			attrNames.forEach(it -> parser.handler.attributeExpr(it[0], it[1]));
		}
	}

}
