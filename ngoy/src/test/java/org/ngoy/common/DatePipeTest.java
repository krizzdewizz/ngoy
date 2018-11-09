package org.ngoy.common;

import static org.assertj.core.api.Assertions.assertThat;
import static org.ngoy.core.Provider.useValue;

import java.util.Locale;

import org.junit.Test;
import org.ngoy.ANgoyTest;
import org.ngoy.common.DatePipe;
import org.ngoy.core.Component;

public class DatePipeTest extends ANgoyTest {

	@Component(selector = "test", declarations = { DatePipe.class }, template = "{{ T(java.time.LocalDateTime).of(2018, 10, 28, 12, 44) | date:'MMMM' }}")
	public static class Cmp {
	}

	@Test
	public void test() {
		assertThat(render(Cmp.class)).isEqualTo("October");
	}

//

	@Test
	public void testGerman() {
		assertThat(render(Cmp.class, useValue(Locale.class, Locale.GERMAN))).isEqualTo("Oktober");
	}
}
