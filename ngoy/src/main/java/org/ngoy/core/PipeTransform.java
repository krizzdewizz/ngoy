package org.ngoy.core;

import org.ngoy.internal.util.Nullable;

public interface PipeTransform {
	/**
	 * @param obj if null, implementors should return null
	 */
	@Nullable
	Object transform(@Nullable Object obj, Object... params);
}
