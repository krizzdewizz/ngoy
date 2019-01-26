package ngoy.core.cot;

import java.lang.invoke.MethodHandle;

public class ReflectBinding {

	public final String name;
	public final MethodHandle getter;

	public ReflectBinding(String name, MethodHandle getter) {
		this.name = name;
		this.getter = getter;
	}

	public Object getValue(Object instance) throws Throwable {
		return getter.invoke(instance);
	}

}