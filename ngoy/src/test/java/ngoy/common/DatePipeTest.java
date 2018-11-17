package ngoy.common;

import static ngoy.core.Provider.useValue;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.Locale;

import org.junit.Test;

import ngoy.ANgoyTest;
import ngoy.core.Component;
import ngoy.core.LocaleProvider;

public class DatePipeTest extends ANgoyTest {

	@Component(selector = "test", template = "{{ T(java.time.LocalDateTime).of(2018, 10, 28, 12, 44) | date:'MMMM' }}")
	public static class Cmp {
	}

	@Test
	public void testDefault() {
		Locale prevLocale = Locale.getDefault();
		try {
			Locale.setDefault(Locale.ENGLISH);
			assertThat(render(Cmp.class)).isEqualTo("October");
		} finally {
			Locale.setDefault(prevLocale);
		}
	}

	//

	@Test
	public void testGerman() {
		assertThat(render(Cmp.class, useValue(LocaleProvider.class, new LocaleProvider.Default(Locale.FRENCH)))).isEqualTo("octobre");
	}
}
