package org.ngoy.translate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.ngoy.core.Provider.useValue;

import java.util.Locale;

import org.junit.Test;
import org.ngoy.ANgoyTest;
import org.ngoy.core.Component;
import org.ngoy.core.LocaleProvider;
import org.ngoy.core.NgModule;

public class TranslatePipeTest extends ANgoyTest {

	@Component(selector = "test", template = "{{ 'MSG_HELLO' | translate }}")
	@NgModule(imports = { TranslateModule.class })
	public static class CmpNotFound {
	}

	@Test
	public void testNotFound() {
		assertThat(render(CmpNotFound.class)).isEqualTo("MSG_HELLO");
	}

	//

	@Component(selector = "test", template = "{{ 'MSG_HELLO' | translate }} {{ 'MSG_QBERT' | translate:'cool' }}")
	@NgModule(imports = { TranslateModule.class })
	public static class Cmp {
		public Cmp(TranslateService service) {
			service.setBundle(TranslatePipeTest.class.getPackage()
					.getName() + ".messages");
		}
	}

	@Test
	public void test() {
		assertThat(render(Cmp.class, useValue(LocaleProvider.class, new LocaleProvider.Default(Locale.GERMAN)))).isEqualTo("hallo qbert ist cool ist qbert.");
	}
}
