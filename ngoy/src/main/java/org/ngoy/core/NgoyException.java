package org.ngoy.core;

import static java.lang.String.format;

public class NgoyException extends RuntimeException {

	public static RuntimeException wrap(Throwable cause) {
		if (cause instanceof RuntimeException) {
			return (RuntimeException) cause;
		}

		return new NgoyException(cause);
	}

	private static final long serialVersionUID = 1L;

	public NgoyException(String message, Throwable cause) {
		super(message, cause);
	}

	public NgoyException(String message, Object... args) {
		super(format(message, args));
	}

	private NgoyException(Throwable cause) {
		super(cause);
	}

}
