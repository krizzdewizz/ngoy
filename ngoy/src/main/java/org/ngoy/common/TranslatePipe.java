package org.ngoy.common;

import org.ngoy.core.Inject;
import org.ngoy.core.Pipe;
import org.ngoy.core.PipeTransform;
import org.ngoy.internal.util.Nullable;

@Pipe("translate")
public class TranslatePipe implements PipeTransform {

	@Inject
	public TranslateService translateService;

	@Override
	public Object transform(@Nullable Object msg, Object... params) {
		if (msg == null) {
			return null;
		}

		return translateService.translate(String.valueOf(msg), params);
	}
}
