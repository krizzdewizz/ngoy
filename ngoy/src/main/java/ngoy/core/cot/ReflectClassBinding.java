package ngoy.core.cot;

import java.lang.invoke.MethodHandle;

public class ReflectClassBinding extends ReflectBinding {

	public ReflectClassBinding(String name, MethodHandle getter) {
		super(name, getter);
	}

	public Boolean getValue(Object instance) throws Throwable {
		return (Boolean) super.getValue(instance);
	}
}