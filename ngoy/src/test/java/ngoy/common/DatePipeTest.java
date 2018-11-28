package ngoy.common;

import static ngoy.core.Provider.useValue;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.Locale;

import org.junit.Before;
import org.junit.Test;

import ngoy.ANgoyTest;
import ngoy.core.LocaleProvider;

public class DatePipeTest extends ANgoyTest {

	private Class<?> cmp;

	@Before
	public void beforeEach() {
		cmp = defineCmp("{{ T(java.time.LocalDateTime).of(2018, 10, 28, 12, 44) | date:'MMMM' }}");
	}

	@Test
	public void testDefault() {
		Locale prevLocale = Locale.getDefault();
		try {
			Locale.setDefault(Locale.ENGLISH);
			assertThat(render(cmp)).isEqualTo("October");
		} finally {
			Locale.setDefault(prevLocale);
		}
	}

	@Test
	public void testGerman() {
		assertThat(render(cmp, useValue(LocaleProvider.class, new LocaleProvider.Default(Locale.FRENCH)))).isEqualTo("octobre");
	}

	@Test
	public void testNoPattern() {
		assertThat(render("{{ T(java.time.LocalDateTime).of(2018, 10, 28, 12, 44) | date }}")).isEqualTo("28.10.2018 12:44:00");
	}

	@Test
	public void testNull() {
		assertThat(render("{{ null | date }}")).isEqualTo("");
	}
}
