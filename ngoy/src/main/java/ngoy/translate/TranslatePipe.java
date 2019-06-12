package ngoy.translate;

import ngoy.core.Inject;
import ngoy.core.Nullable;
import ngoy.core.Pipe;
import ngoy.core.PipeTransform;

/**
 * Transforms a translation key into the translated message.
 * <p>
 * Example:
 *
 * <pre>
 * &lt;a&gt;{{'MSG_HELLO' | translate}}&lt;/a&gt;
 * </pre>
 *
 * @author krizz
 */
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
