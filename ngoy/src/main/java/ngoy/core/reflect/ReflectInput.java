package ngoy.core.reflect;

import ngoy.internal.parser.Inputs;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.util.HashMap;
import java.util.Map;

import static ngoy.core.Util.isSet;
import static ngoy.internal.parser.Inputs.fieldName;

public interface ReflectInput {
    void apply(Object instance, Object value) throws Throwable;

    static Map<String, ReflectInput> of(Class<?> clazz) {
        Map<String, ReflectInput> map = new HashMap<>();
        Lookup lookup = MethodHandles.lookup();
        Inputs.withFieldInputs(clazz, (field, input) -> {
            MethodHandle setter = lookup.unreflectSetter(field);
            ReflectInput ri = (instance, value) -> setter.invoke(instance, value);
            map.put(field.getName(), ri);
            String alias = input.value();
            if (isSet(alias)) {
                map.put(alias, ri);
            }
        });

        Inputs.withMethodInputs(clazz, (meth, input) -> {
            MethodHandle methHandle = lookup.unreflect(meth);
            ReflectInput ri = (instance, value) -> methHandle.invoke(instance, value);
            map.put(fieldName(meth.getName()), ri);
            String alias = input.value();
            if (isSet(alias)) {
                map.put(alias, ri);
            }
        });
        return map;
    }
}