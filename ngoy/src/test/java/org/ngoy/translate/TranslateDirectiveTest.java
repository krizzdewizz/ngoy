package org.ngoy.translate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.ngoy.core.Provider.useValue;

import java.util.Locale;

import org.junit.Test;
import org.ngoy.ANgoyTest;
import org.ngoy.core.Component;
import org.ngoy.core.NgModule;

public class TranslateDirectiveTest extends ANgoyTest {

	@Component(selector = "test", template = "<a translate=\"MSG_HELLO\">x</a>")
	@NgModule(imports = { TranslateModule.class })
	public static class Cmp {
		public Cmp(TranslateService service) {
			service.setBundle(TranslateDirectiveTest.class.getPackage()
					.getName() + ".messages");
		}
	}

	@Test
	public void test() {
		assertThat(render(Cmp.class, useValue(Locale.class, Locale.GERMAN))).isEqualTo("<a>hallo</a>");
	}
}
