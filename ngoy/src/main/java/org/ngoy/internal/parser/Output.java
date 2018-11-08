package org.ngoy.internal.parser;

import org.ngoy.internal.util.Nullable;

public interface Output {
	/**
	 * @param contentType
	 *            null to use default
	 */
	void print(String text, boolean isExpr, boolean escape, @Nullable String contentType);

	void flush();
}
