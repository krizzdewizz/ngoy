package ngoy.translate;

import ngoy.core.Inject;
import ngoy.core.LocaleProvider;

import java.text.MessageFormat;
import java.util.Locale;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;

/**
 * Service for translation/message bundles.
 * <p>
 * Call {@link #setBundle(String)} to load the bundle. Call
 * {@link #translate(String, Object...)} to translate a message into the current
 * locale.
 * <p>
 * If the locale changes, the bundle is reloaded.
 * <p>
 * A warning is printed if a translation is requested without having the bundle
 * configured.
 *
 * @author krizz
 * @see LocaleProvider
 */
public class TranslateService {

    @Inject
    public LocaleProvider localeProvider;

    private ResourceBundle bundle;
    private boolean hadMissingBundleWarning;

    private String bundleBaseName;
    private Locale prevLocale;

    /**
     * Translates the given message key.
     *
     * @param messageKey
     * @param params
     * @return Translated message
     * @see MessageFormat
     */
    public String translate(String messageKey, Object... params) {
        loadBundle();
        missingBundleWarning();
        return bundle != null && bundle.containsKey(messageKey) ? format(bundle.getString(messageKey), params) : messageKey;
    }

    private void missingBundleWarning() {
        if (hadMissingBundleWarning) {
            return;
        }

        if (bundle == null) {
            System.out.println("TranslateService: no bundle has been set. See Config.translateBundle or inject TranslateService into your app and call setBundle() yourself.");
            hadMissingBundleWarning = true;
        }
    }

    private String format(String string, Object... params) {
        if (params.length == 0) {
            return string;
        }
        return MessageFormat.format(string, params);
    }

    /**
     * Sets the required bundle base name.
     *
     * @param baseName See also
     *                 {@link PropertyResourceBundle#getBundle(String, Locale)}
     */
    public void setBundle(String baseName) {
        this.bundleBaseName = baseName;
    }

    private void loadBundle() {

        if (bundleBaseName == null) {
            return;
        }

        Locale localeNow = localeProvider.getLocale();
        if (bundle == null || !localeNow.equals(prevLocale)) {
            bundle = ResourceBundle.getBundle(bundleBaseName, localeNow);
            prevLocale = localeNow;
        }
    }
}
