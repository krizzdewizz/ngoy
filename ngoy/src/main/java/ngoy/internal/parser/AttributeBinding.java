package ngoy.internal.parser;

import jodd.jerry.Jerry;
import jodd.lagarto.dom.Attribute;
import ngoy.core.HostBinding;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static java.lang.String.format;
import static ngoy.core.dom.XDom.getAttributes;
import static ngoy.internal.parser.ExprParser.convertPipesToTransformCalls;

public class AttributeBinding {

    public static final String BINDING_CLASS = "class.";
    public static final String BINDING_ATTR = "attr.";
    private static final String BINDING_TEXT = "ngText";
    public static final String BINDING_STYLE = "style.";
    public static final String BINDING_NG_CLASS = "ngClass";
    public static final String BINDING_NG_STYLE = "ngStyle";

    static class HostBindings extends LinkedHashMap<String, String> {
        private static final long serialVersionUID = 1L;
        boolean hasClass;
        boolean hasStyle;

        private void add(HostBinding binding, String expr) {
            String value = binding.value();
            if (value.startsWith(BINDING_CLASS) || value.equals(BINDING_NG_CLASS)) {
                hasClass = true;
            }
            if (value.startsWith(BINDING_STYLE) || value.equals(BINDING_NG_STYLE)) {
                hasStyle = true;
            }
            put(format("[%s]", value), expr);
        }
    }

    private static void addAttributeBinding(Parser parser, String name, String expr, Set<String> exclude, List<String[]> targetAttrNames, List<String[]> targetClassNames,
                                            List<String[]> targetStyleNames) {

        String rawName = name.substring(1, name.length() - 1);

        if (exclude.contains(rawName.toLowerCase())) {
            return;
        }

        expr = convertPipesToTransformCalls(expr, parser.resolver);

        if (rawName.equals(BINDING_NG_CLASS)) {
            targetClassNames.add(new String[]{rawName, expr});
        } else if (rawName.equals(BINDING_NG_STYLE)) {
            targetStyleNames.add(new String[]{rawName, expr});
        } else if (rawName.startsWith(BINDING_CLASS)) {
            String className = rawName.substring(BINDING_CLASS.length());
            targetClassNames.add(new String[]{className, expr});
        } else if (rawName.startsWith(BINDING_ATTR)) {
            String attrName = rawName.substring(BINDING_ATTR.length());
            targetAttrNames.add(new String[]{attrName, expr});
        } else if (rawName.startsWith(BINDING_STYLE)) {
            String attrName = rawName.substring(BINDING_STYLE.length());
            targetStyleNames.add(new String[]{attrName, expr});
        } else if (rawName.equals(BINDING_TEXT)) {
            parser.handler.textOverride(expr);
        } else {
            parser.handler.attributeExpr(rawName, expr);
        }
    }

    static void replaceAttrs(Parser parser, Jerry el, Set<String> excludeBindings, List<String[]> targetAttrNames, List<String[]> targetClassNames, List<String[]> targetStyleNames) {
        for (Attribute attr : getAttributes(el)) {
            String name = attr.getName();
            if (name.equals("class") || name.equals("style") || name.startsWith("*")) {
                continue;
            }

            if (name.startsWith("[")) {
                if (!name.endsWith("]")) {
                    throw new ParseException("Attribute binding malformed: missing ].", el);
                }
                addAttributeBinding(parser, name, attr.getValue(), excludeBindings, targetAttrNames, targetClassNames, targetStyleNames);
            } else if (!excludeBindings.contains(name)) {
                boolean hasValue = attr.getValue() != null;
                parser.handler.attributeStart(name, hasValue);
                if (hasValue) {
                    parser.replaceExpr(attr.getValue());
                    parser.handler.attributeEnd();
                }
            }
        }

        replaceAttrExpr(parser, targetAttrNames, targetClassNames, targetStyleNames);
    }

    static HostBindings getHostBindings(Class<?> cmpClass) {
        HostBindings result = new HostBindings();
        for (Field field : cmpClass.getFields()) {
            HostBinding binding = field.getAnnotation(HostBinding.class);
            if (binding == null) {
                continue;
            }

            result.add(binding, field.getName());
        }

        for (Method meth : cmpClass.getMethods()) {
            HostBinding binding = meth.getAnnotation(HostBinding.class);
            if (binding == null) {
                continue;
            }

            if (meth.getParameterCount() > 0) {
                throw new ParseException("Host binding method must not have parameters: %s.%s", cmpClass.getName(), meth.getName());
            }

            result.add(binding, format("%s()", meth.getName()));
        }

        return result;
    }

    static void addHostAttributeBindings(Parser parser, HostBindings bindings, Set<String> excludeBindings, List<String[]> attrNames, List<String[]> classNames, List<String[]> styleNames) {
        for (Map.Entry<String, String> binding : bindings.entrySet()) {
            addAttributeBinding(parser, binding.getKey(), binding.getValue(), excludeBindings, attrNames, classNames, styleNames);
        }

        replaceAttrExpr(parser, attrNames, classNames, styleNames);
    }

    private static void replaceAttrExpr(Parser parser, List<String[]> attrNames, List<String[]> classNames, List<String[]> styleNames) {
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
