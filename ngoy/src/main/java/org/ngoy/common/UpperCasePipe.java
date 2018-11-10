package org.ngoy.common;

import java.util.Locale;

import org.ngoy.core.Inject;
import org.ngoy.core.Nullable;
import org.ngoy.core.Pipe;
import org.ngoy.core.PipeTransform;

@Pipe("uppercase")
public class UpperCasePipe implements PipeTransform {

	@Inject
	public Locale locale;

	@Override
	public Object transform(@Nullable Object obj, Object... params) {
		if (obj == null) {
			return null;
		}

		return obj.toString()
				.toUpperCase(locale);
	}

}
