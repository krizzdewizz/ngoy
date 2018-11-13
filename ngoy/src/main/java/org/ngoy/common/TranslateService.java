package org.ngoy.common;

import java.text.MessageFormat;
import java.util.Locale;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;

import org.ngoy.core.Inject;
import org.ngoy.core.LocaleProvider;

public class TranslateService {

	@Inject
	public LocaleProvider locale;

	private ResourceBundle bundle;
	private boolean hadMissingBundleWarning;

	private String bundleBaseName;
	private Locale prevLocale;

	public String translate(String msg, Object... params) {
		loadBundle();
		missingBundleWarning();
		return bundle != null && bundle.containsKey(msg) ? format(bundle.getString(msg), params) : msg;
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

	public void setBundle(String baseName) {
		this.bundleBaseName = baseName;
	}

	private void loadBundle() {
		Locale localeNow = locale.get();
		if (bundle == null || !localeNow.equals(prevLocale)) {
			bundle = PropertyResourceBundle.getBundle(bundleBaseName, localeNow);
			prevLocale = localeNow;
		}
	}
}
