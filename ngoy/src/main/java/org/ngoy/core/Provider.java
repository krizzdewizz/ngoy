package org.ngoy.core;

import static java.lang.String.format;
import static java.util.stream.Collectors.toList;

import java.util.stream.Stream;

public class Provider {

	public static Provider[] of(Class<?>... clazz) {
		return Stream.of(clazz)
				.map(Provider::of)
				.collect(toList())
				.toArray(new Provider[0]);
	}

	public static <P> Provider of(Class<P> clazz) {
		return useClass(clazz, clazz);
	}

	public static <P, C extends P> Provider useClass(Class<P> provide, Class<C> useClass) {
		return new Provider(provide, useClass != null ? useClass : provide, null);
	}

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
