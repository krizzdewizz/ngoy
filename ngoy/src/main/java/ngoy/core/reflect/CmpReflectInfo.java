package ngoy.core.reflect;

import ngoy.core.Component;
import ngoy.core.HostBinding;
import ngoy.internal.parser.AttributeBinding;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import static ngoy.core.NgoyException.wrap;

public class CmpReflectInfo {

    public static CmpReflectInfo of(Class<?> cmpClass) {
        String selector = cmpClass.getAnnotation(Component.class)
                .selector();

        Lookup lookup = MethodHandles.lookup();

        Set<ReflectBinding> attrBindings = new LinkedHashSet<>();
        Set<ReflectBinding> classBindings = new LinkedHashSet<>();
        Set<ReflectBinding> styleBindings = new LinkedHashSet<>();

        withFieldHostBindings(cmpClass, makeAddCb(lookup::unreflectGetter, attrBindings, classBindings, styleBindings));
        withMethodHostBindings(cmpClass, makeAddCb(lookup::unreflect, attrBindings, classBindings, styleBindings));

        return new CmpReflectInfo(selector, ReflectInput.of(cmpClass), attrBindings, classBindings, styleBindings);
    }

    private interface Unreflect<T> {
        MethodHandle run(T t) throws Exception;
    }

    private static <T> HostBindingCallback<T> makeAddCb(Unreflect<T> unref, Set<ReflectBinding> attrBindings, Set<ReflectBinding> classBindings, Set<ReflectBinding> styleBindings) {
        return (f, name, prefix) -> {
            if (prefix.equals(AttributeBinding.BINDING_ATTR)) {
                attrBindings.add(new ReflectAttrBinding(name, unref.run(f)));
            } else if (prefix.equals(AttributeBinding.BINDING_CLASS)) {
                classBindings.add(new ReflectClassBinding(name, unref.run(f)));
            } else if (prefix.equals(AttributeBinding.BINDING_STYLE)) {
                styleBindings.add(new ReflectStyleBinding(name, unref.run(f)));
            } else if (name.equals(AttributeBinding.BINDING_NG_CLASS)) {
                classBindings.add(new ReflectClassBinding("class", unref.run(f)));
            } else if (name.equals(AttributeBinding.BINDING_NG_STYLE)) {
                styleBindings.add(new ReflectStyleBinding("style", unref.run(f)));
            }
        };
    }

    public interface HostBindingCallback<T> {
        void run(T fieldOrMethod, String name, String prefix) throws Exception;
    }

    public static void withFieldHostBindings(Class<?> cmpClass, HostBindingCallback<Field> cb) {
        for (Field f : cmpClass.getFields()) {
            HostBinding hb = f.getAnnotation(HostBinding.class);
            if (hb != null) {
                runCb(cb, f, hb.value());
            }
        }
    }

    public static void withMethodHostBindings(Class<?> cmpClass, HostBindingCallback<Method> cb) {
        for (Method m : cmpClass.getMethods()) {
            HostBinding hb = m.getAnnotation(HostBinding.class);
            if (hb != null) {
                runCb(cb, m, hb.value());
            }
        }
    }

    private static <T> void runCb(HostBindingCallback<T> cb, T f, String binding) {
        try {
            int pos = binding.indexOf('.');
            String name;
            String prefix;
            if (pos < 0) {
                name = binding;
                prefix = "";
            } else {
                name = binding.substring(pos + 1);
                prefix = binding.substring(0, pos + 1); // see AttributeBinding
            }

            cb.run(f, name, prefix);
        } catch (Exception e) {
            throw wrap(e);
        }
    }

    public final String selector;
    public final Map<String, ReflectInput> inputs;
    public final Set<ReflectBinding> attrBindings;
    public final Set<ReflectBinding> classBindings;
    public final Set<ReflectBinding> styleBindings;

    private CmpReflectInfo(String selector, Map<String, ReflectInput> inputs, Set<ReflectBinding> attrBindings, Set<ReflectBinding> classBindings, Set<ReflectBinding> styleBindings) {
        this.selector = selector;
        this.inputs = inputs;
        this.attrBindings = attrBindings;
        this.classBindings = classBindings;
        this.styleBindings = styleBindings;
    }
}