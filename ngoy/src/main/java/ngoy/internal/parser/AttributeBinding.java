package ngoy.internal.parser;

import static java.lang.String.format;
import static ngoy.core.dom.XDom.getAttributes;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Set;

import jodd.jerry.Jerry;
import jodd.lagarto.dom.Attribute;
import ngoy.core.HostBinding;

public class AttributeBinding {

	private static final String BINDING_CLASS = "class.";
	private static final String BINDING_ATTR = "attr.";
	private static final String BINDING_TEXT = "ngText";
	private static final String BINDING_STYLE = "style.";

	static void addAttributeBinding(Parser parser, String name, String expr, Set<String> exclude, List<String[]> targetClassNames, List<String[]> targetAttrNames, List<String[]> targetStyleNames) {

		String rawName = name.substring(1, name.length() - 1);

		if (exclude.contains(rawName.toLowerCase())) {
			return;
		}
		
		expr = ExprParser.convertPipesToTransformCalls(expr, parser.resolver);

		if (rawName.equals("ngClass")) {
			targetClassNames.add(new String[] { rawName, expr });
		} else if (rawName.equals("ngStyle")) {
			targetStyleNames.add(new String[] { rawName, expr });
		} else if (rawName.startsWith(BINDING_CLASS)) {
			String className = rawName.substring(BINDING_CLASS.length());
			targetClassNames.add(new String[] { className, expr });
		} else if (rawName.startsWith(BINDING_ATTR)) {
			String attrName = rawName.substring(BINDING_ATTR.length());
			targetAttrNames.add(new String[] { attrName, expr });
		} else if (rawName.startsWith(BINDING_STYLE)) {	
			String attrName = rawName.substring(BINDING_STYLE.length());
			targetStyleNames.add(new String[] { attrName, expr });
		} else if (rawName.equals(BINDING_TEXT)) {
			parser.handler.textOverride(expr);
		} else {
			parser.handler.attributeExpr(rawName, expr);
		}
	}

	static void replaceAttrs(Parser parser, Jerry el, Set<String> excludeBindings, List<String[]> targetClassNames, List<String[]> targetAttrNames, List<String[]> targetStyleNames) {
		for (Attribute attr : getAttributes(el)) {
			String name = attr.getName();
			if (name.equals("class") || name.equals("style") || name.startsWith("*")) {
				continue;
			}

			if (name.startsWith("[")) {
				if (!name.endsWith("]")) {
					throw new ParseException("Attribute binding malformed: missing ].", el);
				}
				addAttributeBinding(parser, name, attr.getValue(), excludeBindings, targetClassNames, targetAttrNames, targetStyleNames);
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

	static void addHostAttributeBindings(Parser parser, Class<?> cmpClass, Set<String> excludeBindings, List<String[]> classNames, List<String[]> attrNames, List<String[]> styleNames) {
		for (Field f : cmpClass.getFields()) {
			HostBinding hb = f.getAnnotation(HostBinding.class);
			if (hb == null) {
				continue;
			}

			addAttributeBinding(parser, format("[%s]", hb.value()), f.getName(), excludeBindings, classNames, attrNames, styleNames);
		}

		for (Method m : cmpClass.getMethods()) {
			HostBinding hb = m.getAnnotation(HostBinding.class);
			if (hb == null) {
				continue;
			}

			if (m.getParameterCount() > 0) {
				throw new ParseException("Host binding method must not have parameters: %s.%s", cmpClass.getName(), m.getName());
			}

			addAttributeBinding(parser, format("[%s]", hb.value()), format("%s()", m.getName()), excludeBindings, classNames, attrNames, styleNames);
		}
	}

	static void replaceAttrExpr(Parser parser, List<String[]> classNames, List<String[]> attrNames, List<String[]> styleNames) {
		if (!classNames.isEmpty()) {
			parser.handler.attributeClasses(classNames);
		}

		if (!styleNames.isEmpty()) {
			parser.handler.attributeStyles(styleNames);
		}

		if (!attrNames.isEmpty()) {
			attrNames.forEach(it -> parser.handler.attributeExpr(it[0], it[1]));
		}
	}
}
