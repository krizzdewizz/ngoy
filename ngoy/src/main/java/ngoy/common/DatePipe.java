package ngoy.common;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import ngoy.core.Inject;
import ngoy.core.LocaleProvider;
import ngoy.core.Nullable;
import ngoy.core.Pipe;
import ngoy.core.PipeTransform;

@Pipe("date")
public class DatePipe implements PipeTransform {

	@Inject
	public LocaleProvider locale;

	@Override
	public Object transform(@Nullable Object obj, Object... params) {
		if (obj == null) {
			return null;
		}
		String pattern = params.length == 0 ? "dd.MM.yyyy hh:mm:ss" : params[0].toString();
		LocalDateTime localDate = (LocalDateTime) obj;
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern, locale.get());
		return localDate.format(formatter);
	}
}
