package ngoy.common;

import static ngoy.core.Provider.useValue;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.Locale;

import org.junit.Test;

import ngoy.ANgoyTest;
import ngoy.core.Component;
import ngoy.core.LocaleProvider;

public class DatePipeTest extends ANgoyTest {

	@Component(selector = "", template = "{{ java.time.LocalDateTime.of(2018, 10, 28, 12, 44) | date:\"MMMM\" }}")
	public static class DateCmp {
	}

	@Test
	public void testDefault() {
		Locale prevLocale = Locale.getDefault();
		try {
			Locale.setDefault(Locale.ENGLISH);
			assertThat(render(DateCmp.class)).isEqualTo("October");
		} finally {
			Locale.setDefault(prevLocale);
		}
	}

	@Test
	public void testGerman() {
		assertThat(render(DateCmp.class, useValue(LocaleProvider.class, new LocaleProvider.Default(Locale.FRENCH)))).isEqualTo("octobre");
	}

	//

	@Component(selector = "", template = "{{ java.time.LocalDateTime.of(2018, 10, 28, 12, 44) | date }}")
	public static class NoPatternCmp {
	}

	@Test
	public void testNoPattern() {
		assertThat(render(NoPatternCmp.class)).isEqualTo("28.10.2018 12:44:00");
	}

	//

	@Component(selector = "", template = "{{ null | date }}")
	public static class NullCmp {
	}

	@Test
	public void testNull() {
		assertThat(render(NullCmp.class)).isEqualTo("");
	}
}
