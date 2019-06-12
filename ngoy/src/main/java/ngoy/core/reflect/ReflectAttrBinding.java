package ngoy.core.reflect;

import java.lang.invoke.MethodHandle;

public class ReflectAttrBinding extends ReflectBinding {

    public ReflectAttrBinding(String name, MethodHandle getter) {
        super((char) 0, (char) 0, name, getter);
    }
}