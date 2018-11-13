package org.ngoy.translate;

import org.ngoy.core.Inject;
import org.ngoy.core.Nullable;
import org.ngoy.core.Pipe;
import org.ngoy.core.PipeTransform;

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
