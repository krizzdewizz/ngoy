package ngoy.common;

import ngoy.core.Inject;
import ngoy.core.LocaleProvider;
import ngoy.core.NgoyException;
import ngoy.core.Nullable;
import ngoy.core.Optional;
import ngoy.core.Pipe;
import ngoy.core.PipeTransform;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;

import static java.util.Arrays.asList;
import static ngoy.core.Util.isSet;

/**
 * Formats a {@link LocalDateTime}, {@link LocalDate} or {@link Date}/Long
 * instance in the current locale, accepting an optional format pattern as the
 * first argument.
 * <p>
 * Another {@link Config} may be provided to override defaults.
 * <p>
 * Example:
 * <p>
 *
 * <pre>
 *
 * {{ java.time.LocalDateTime.of(2018, 10, 28, 12, 44) | date:'MMMM' }}
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

    /**
     * Provide a Config to override default format patterns.
     */
    public static class Config {

        /**
         * Pattern used to format {@link Date}/Long instances.
         * <p>
         * If null or empty, uses {@link SimpleDateFormat#getDateTimeInstance()}
         */
        public String defaultDatePattern;

        /**
         * Pattern used to format {@link LocalDate} instances.
         * <p>
         * If null or empty, uses {@link DateTimeFormatter#ISO_LOCAL_DATE}
         */
        public String defaultLocalDatePattern;

        /**
         * Pattern used to format {@link LocalDateTime} instances.
         * <p>
         * If null or empty, uses {@link DateTimeFormatter#ISO_LOCAL_DATE_TIME}
         */
        public String defaultLocalDateTimePattern;
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
            return formatter(formatPattern(params, config.defaultLocalDateTimePattern), DateTimeFormatter.ISO_LOCAL_DATE_TIME).format((LocalDateTime) obj);
        } else if (obj instanceof LocalDate) {
            return formatter(formatPattern(params, config.defaultLocalDatePattern), DateTimeFormatter.ISO_LOCAL_DATE).format((LocalDate) obj);
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
        return (isSet(pattern) ? new SimpleDateFormat(pattern, localeProvider.getLocale()) : DateFormat.getDateTimeInstance()).format(date);
    }

    private DateTimeFormatter formatter(String pattern, DateTimeFormatter def) {
        return isSet(pattern) ? DateTimeFormatter.ofPattern(pattern, localeProvider.getLocale()) : def;
    }

    private String formatPattern(Object[] params, String def) {
        return params.length == 0 ? def : params[0].toString();
    }
}
