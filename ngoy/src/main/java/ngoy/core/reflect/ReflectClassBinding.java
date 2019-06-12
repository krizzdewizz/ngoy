package ngoy.core.reflect;

import ngoy.core.NgoyException;

import java.lang.invoke.MethodHandle;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

public class ReflectClassBinding extends ReflectBinding {

    public ReflectClassBinding(String name, MethodHandle getter) {
        super(' ', (char) 0, name, getter);
    }

    public Object getValue(Object instance) throws Throwable {
        Object val = super.getValue(instance);
        if (val == null) {
            return null;
        }

        if (val instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, Boolean> map = (Map<String, Boolean>) val;
            Map<String, Boolean> newMap = new LinkedHashMap<>();
            // exclude false values
            for (Entry<String, Boolean> entry : map.entrySet()) {
                Boolean value = entry.getValue();
                if (value != null && value) {
                    newMap.put(entry.getKey(), true);
                }
            }
            return newMap;
        }

        if (!(val instanceof Boolean)) {
            throw new NgoyException("Type of class host binding must be boolean");
        }

        boolean bool = (boolean) val;
        return bool ? true : null;
    }
}