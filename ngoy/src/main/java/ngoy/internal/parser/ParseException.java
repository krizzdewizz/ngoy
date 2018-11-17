package ngoy.internal.parser;

import ngoy.core.NgoyException;

public class ParseException extends NgoyException {

	private static final long serialVersionUID = 1L;

	public ParseException(String message, Object... params) {
		super(message, params);
	}
}
