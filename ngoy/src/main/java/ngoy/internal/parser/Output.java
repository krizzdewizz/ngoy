package ngoy.internal.parser;

import ngoy.core.Nullable;

public interface Output {
	/**
	 * @param contentType
	 *            null to use default
	 */
	void print(String text, boolean isExpr, boolean escape, @Nullable String contentType);

	void flush();
}
