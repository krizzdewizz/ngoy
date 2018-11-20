package ngoy.core;

import static java.lang.String.format;
import static java.util.stream.Collectors.toList;

import java.util.stream.Stream;

/**
 * Describes what to inject when an instance of a given class is requested by
 * the injector.
 * <p>
 * There are 3 kinds:
 * <ul>
 * <li>{@link #of(Class)}, {@link #of(Class...)} -&gt; return an instance of
 * that class.</li>
 * <li>{@link #useClass(Class, Class)} -&gt; If class <code>provide</code> is
 * requested, return an instance of class <code>useClass</code>. This allows to
 * inject different behaviours.</li>
 * <li>{@link #useValue(Class, Object)} -&gt; If class <code>provide</code> is
 * requested, return the instance <code>useValue</code>. This allows to inject
 * instances from the 'outer environment'.</li>
 * </ul>
 * 
 * @author krizz
 */
public class Provider {

	/**
	 * Returns providers for the given classes.
	 * 
	 * @param classes
	 * @return Providers
	 */
	public static Provider[] of(Class<?>... classes) {
		return Stream.of(classes)
				.map(Provider::of)
				.collect(toList())
				.toArray(new Provider[0]);
	}

	/**
	 * Returns a provider for the given class.
	 * 
	 * @param clazz
	 * @return Provider
	 */
	public static <P> Provider of(Class<P> clazz) {
		return useClass(clazz, clazz);
	}

	/**
	 * Returns a provider, instantiating from <code>useClass</code>.
	 * 
	 * @param provide
	 * @return Provider
	 */
	public static <P, C extends P> Provider useClass(Class<P> provide, Class<C> useClass) {
		return new Provider(provide, useClass != null ? useClass : provide, null);
	}

	/**
	 * Provides <code>useValue</code>.
	 * 
	 * @param provide
	 * @param useValue
	 * @return Provider
	 */
	public static <P, V extends P> Provider useValue(Class<P> provide, V useValue) {
		return new Provider(provide, null, useValue);
	}

	private final Class<?> provide;
	private final Class<?> useClass;
	private final Object useValue;

	/**
	 * @param useValue if non-null, uses that instead of useClass
	 */
	private Provider(Class<?> provide, Class<?> useClass, @Nullable Object useValue) {
		this.provide = provide;
		this.useClass = useClass;
		this.useValue = useValue;
	}

	public Class<?> getProvide() {
		return provide;
	}

	/**
	 * @return Non-null to use that instance. Null to use <code>useClass</code>.
	 */
	@Nullable
	public Object getUseValue() {
		return useValue;
	}

	public Class<?> getUseClass() {
		return useClass;
	}

	@Override
	public String toString() {
		Class<?> target = useValue != null ? useValue.getClass() : useClass;
		String targetType = useValue != null ? "useValue" : "useClass";
		return format("%s -> %s: %s", provide.getName(), targetType, target.getName());
	}
}
