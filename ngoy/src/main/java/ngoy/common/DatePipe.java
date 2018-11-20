package ngoy.common;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import ngoy.core.Inject;
import ngoy.core.LocaleProvider;
import ngoy.core.Nullable;
import ngoy.core.Pipe;
import ngoy.core.PipeTransform;

/**
 * Formats a {@link LocalDateTime} instance in the current locale, accepting an
 * optional format pattern as the first argument.
 * <p>
 * Example:
 * <p>
 * 
 * <pre>
 *  
 * {{ T(java.time.LocalDateTime).of(2018, 10, 28, 12, 44) | date:'MMMM' }}
 * </pre>
 * <p>
 * Output:
 * 
 * <pre>
 * October
 * </pre>
 * 
 * @author krizz
 */
@Pipe("date")
public class DatePipe implements PipeTransform {

	private static final String DEFAULT_PATTERN = "dd.MM.yyyy hh:mm:ss";

	@Inject
	public LocaleProvider localeProvider;

	@Override
	public Object transform(@Nullable Object obj, Object... params) {
		if (obj == null) {
			return null;
		}
		String pattern = params.length == 0 ? DEFAULT_PATTERN : params[0].toString();
		LocalDateTime localDate = (LocalDateTime) obj;
		return format(localDate, pattern);
	}

	/**
	 * Formats the given date with the given pattern using the current locale.
	 * 
	 * @param date
	 * @param pattern
	 * @return Formatted date
	 * @see DateTimeFormatter
	 */
	public String format(LocalDateTime date, String pattern) {
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern, localeProvider.getLocale());
		return date.format(formatter);
	}
}
