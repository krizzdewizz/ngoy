package ngoy.core.reflect;

import java.lang.invoke.MethodHandle;

public class ReflectStyleBinding extends ReflectBinding {

	public ReflectStyleBinding(String name, MethodHandle getter) {
		super(';', ':', name, getter);
	}
}