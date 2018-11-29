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
	 * @return for an additional injector: null if none
	 * @throws NgoyException for a root injector if no instance/provider found for
	 *                       class
	 */
	<T> T get(Class<T> clazz);

	/**
	 * Return always a new instance.
	 * 
	 * @return for an additional injector: null if none
	 * @throws NgoyException for a root injector if no instance/provider found for
	 *                       class
	 */
	default <T> T getNew(@SuppressWarnings("unused") Class<T> clazz) {
		return null;
	}
}
