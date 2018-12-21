package ngoy.common;

import static java.lang.String.format;
import static java.util.Arrays.asList;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;

import ngoy.core.Inject;
import ngoy.core.LocaleProvider;
import ngoy.core.NgoyException;
import ngoy.core.Nullable;
import ngoy.core.Optional;
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

	public static class Config {
		public String defaultDatePattern = "dd.MM.yyyy HH:mm:ss";
		public String defaultLocalDatePattern = "dd.MM.yyyy";
		public String defaultLocalDateTimePattern = format("%s hh:mm:ss", defaultLocalDatePattern);
	}

	private static final Config DEFAULT_CONFIG = new Config();

	@Inject
	public LocaleProvider localeProvider;

	@Inject
	@Optional
	public Config config = DEFAULT_CONFIG;

	@Override
	public Object transform(@Nullable Object obj, Object... params) {
		if (obj == null) {
			return null;
		}

		if (obj instanceof LocalDateTime) {
			return formatter(formatPattern(params, config.defaultLocalDateTimePattern)).format((LocalDateTime) obj);
		} else if (obj instanceof LocalDate) {
			return formatter(formatPattern(params, config.defaultLocalDatePattern)).format((LocalDate) obj);
		} else if (obj instanceof Date) {
			return formatDate((Date) obj, params, config.defaultDatePattern);
		} else if (obj instanceof Long) {
			return formatDate(new Date((long) obj), params, config.defaultDatePattern);
		} else {
			throw new NgoyException("DatePipe: input must be one of type %s", asList(LocalDate.class.getName(), LocalDateTime.class.getName(), Date.class.getName(), long.class.getName()));
		}
	}

	private String formatDate(Date date, Object[] params, String defPattern) {
		String pattern = formatPattern(params, defPattern);
		return new SimpleDateFormat(pattern, localeProvider.getLocale()).format(date);
	}

	private DateTimeFormatter formatter(String pattern) {
		return DateTimeFormatter.ofPattern(pattern, localeProvider.getLocale());
	}

	private String formatPattern(Object[] params, String def) {
		return params.length == 0 ? def : params[0].toString();
	}
}
