package ngoy.common;

import ngoy.core.Inject;
import ngoy.core.LocaleProvider;
import ngoy.core.Nullable;
import ngoy.core.Optional;
import ngoy.core.Pipe;
import ngoy.core.PipeTransform;

import java.util.Locale;

/**
 * Capitalizes the input using the current locale.
 * <p>
 * Example:
 * <p>
 *
 * <pre>
 *
 * {{ 'hello' | lowercase }}
 * </pre>
 * <p>
 * Output:
 *
 * <pre>
 * Hello
 * </pre>
 *
 * @author krizz
 */
@Pipe("capitalize")
public class CapitalizePipe implements PipeTransform {

    @Inject
    @Optional
    @Nullable
    public LocaleProvider localeProvider;

    @Override
    public Object transform(@Nullable Object obj, Object... params) {
        if (obj == null) {
            return null;
        }

        String s = obj.toString();
        if (s.isEmpty()) {
            return "";
        }

        String first = s.substring(0, 1);
        return first
                .toUpperCase(localeProvider == null ? Locale.getDefault() : localeProvider.getLocale())
                .concat(s.substring(1));
    }
}
