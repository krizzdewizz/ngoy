package ngoy.core;

/**
 * Returns an instance of a given class, having all the instance's dependencies
 * injected.
 * <p>
 * Ngoy supports constructor injection and field injection through the
 * {@link Inject} annotation. Constructor parameters must not be annotated.
 * 
 * @author krizz
 */
public interface Injector {
	/**
	 * Returns an instance.
	 * 
	 * @param clazz Class to return an instance of
	 * @return for an additional injector: null if none
	 * @throws NgoyException for a root injector if no instance/provider found for
	 *                       class
	 */
	<T> T get(Class<T> clazz);

	/**
	 * Returns always a new instance.
	 * 
	 * @param clazz Class to return a new instance of
	 * @return for an additional injector: null if none
	 * @throws NgoyException for a root injector if no instance/provider found for
	 *                       class
	 */
	default <T> T getNew(Class<T> clazz) {
		return null;
	}

	/**
	 * Returns a new component instance for the given selector.
	 * 
	 * @param selector
	 * @return New component instance
	 * @throws NgoyException for a root injector if no instance/provider found for
	 *                       the selector
	 */
	@Nullable
	default <T> T getNewCmp(String selector) {
		return null;
	};

	/**
	 * Returns the selector of the given component class.
	 * 
	 * @param componentClass Component class
	 * @return selector or null if none
	 */
	@Nullable
	default String getCmpSelector(Class<?> componentClass) {
		return null;
	};
}
