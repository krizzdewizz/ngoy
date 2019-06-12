package ngoy.common;

import ngoy.core.Inject;
import ngoy.core.LocaleProvider;
import ngoy.core.Nullable;
import ngoy.core.Pipe;
import ngoy.core.PipeTransform;

/**
 * Transforms the input to lowercase using the current locale.
 * <p>
 * Example:
 * <p>
 *
 * <pre>
 *
 * {{ 'HELLO' | lowercase }}
 * </pre>
 * <p>
 * Output:
 *
 * <pre>
 * hello
 * </pre>
 *
 * @author krizz
 */
@Pipe("lowercase")
public class LowerCasePipe implements PipeTransform {

    @Inject
    public LocaleProvider localeProvider;

    @Override
    public Object transform(@Nullable Object obj, Object... params) {
        if (obj == null) {
            return null;
        }

        return obj.toString()
                .toLowerCase(localeProvider.getLocale());
    }

}
