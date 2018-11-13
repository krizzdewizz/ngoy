package org.ngoy.core;

import java.util.Locale;

public interface LocaleProvider {
	public class Default implements LocaleProvider {
		private final Locale locale;

		public Default(Locale locale) {
			this.locale = locale;
		}

		public Locale get() {
			return locale;
		}
	}

	Locale get();
}
