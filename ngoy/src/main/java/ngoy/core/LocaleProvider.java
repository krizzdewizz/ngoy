package ngoy.core;

import java.util.Locale;

/**
 * Provides the current locale.
 * <p>
 * Since ngoy's locale is not directly bound to the System's locale, one can
 * render a template quickly in many languages.
 * 
 * @author krizz
 */
public interface LocaleProvider {
	class Default implements LocaleProvider {
		private final Locale locale;

		public Default(Locale locale) {
			this.locale = locale;
		}

		@Override
		public Locale getLocale() {
			return locale;
		}
	}

	Locale getLocale();
}
