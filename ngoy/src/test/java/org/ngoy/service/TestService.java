package org.ngoy.service;

/**
 * Injectable test service that provides a value.
 */
public class TestService<T> {

	public static <T> TestService<T> of(T value) {
		return new TestService<T>(value);
	}

	public final T value;

	private TestService(T value) {
		this.value = value;
	}
}
