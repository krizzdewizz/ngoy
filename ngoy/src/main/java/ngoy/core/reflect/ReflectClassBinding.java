package ngoy.core.reflect;

import java.lang.invoke.MethodHandle;

import ngoy.core.NgoyException;

public class ReflectClassBinding extends ReflectBinding {

	public ReflectClassBinding(String name, MethodHandle getter) {
		super(' ', (char) 0, name, getter);
	}

	public Boolean getValue(Object instance) throws Throwable {
		Object val = super.getValue(instance);
		if (val == null) {
			return null;
		}

		if (!(val instanceof Boolean)) {
			throw new NgoyException("Type of class host binding must be boolean");
		}

		boolean bool = (boolean) val;
		return bool ? bool : null;
	}
}