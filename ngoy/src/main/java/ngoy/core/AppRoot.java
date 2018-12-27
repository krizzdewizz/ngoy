package ngoy.core;

import ngoy.Ngoy;

/**
 * Sub components can inject this interface to gain access to the root app
 * component.
 * 
 * @author krizz
 */
public interface AppRoot {
	/**
	 * @return App component class
	 * @see Ngoy#app(Class)
	 */
	Class<?> getAppClass();

	/**
	 * @return App component instance
	 */
	<T> T getAppInstance();
}
