package org.ngoy.common;

import java.text.MessageFormat;
import java.util.Locale;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;

import org.ngoy.core.Inject;

public class TranslateService {

	@Inject
	public Locale locale;

	private ResourceBundle bundle;
	private boolean hadMissingBundleWarning;

	public String translate(String msg, Object... params) {
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
		this.bundle = PropertyResourceBundle.getBundle(baseName, locale);
	}
}
