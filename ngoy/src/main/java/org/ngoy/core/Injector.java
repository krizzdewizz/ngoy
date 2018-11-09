package org.ngoy.core;

public interface Injector {
	/**
	 * @return for an additional injector: null if none
	 * @throws NgoyException for a root injector and no instance found for class
	 */
	<T> T get(Class<T> clazz);
}
