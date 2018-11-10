package org.ngoy.common;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

import org.ngoy.core.Inject;
import org.ngoy.core.Nullable;
import org.ngoy.core.Pipe;
import org.ngoy.core.PipeTransform;

@Pipe("date")
public class DatePipe implements PipeTransform {

	@Inject
	public Locale locale;

	@Override
	public Object transform(@Nullable Object obj, Object... params) {
		if (obj == null) {
			return null;
		}
		String pattern = params.length == 0 ? "dd.MM.yyyy hh:mm:ss" : params[0].toString();
		LocalDateTime localDate = (LocalDateTime) obj;
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern, locale);
		return localDate.format(formatter);
	}
}
