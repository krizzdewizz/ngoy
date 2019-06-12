package ngoy.translate;

import ngoy.ANgoyTest;
import ngoy.core.Component;
import ngoy.core.LocaleProvider;
import ngoy.core.NgModule;
import org.junit.Test;

import java.util.Locale;

import static ngoy.core.Provider.useValue;
import static org.assertj.core.api.Assertions.assertThat;

public class TranslatePipeTest extends ANgoyTest {

	@Component(selector = "test", template = "{{ \"MSG_HELLO\" | translate }}")
	@NgModule(imports = { TranslateModule.class })
	public static class CmpNotFound {
	}

	@Test
	public void testNotFound() {
		assertThat(render(CmpNotFound.class)).isEqualTo("MSG_HELLO");
	}

	//

	@Component(selector = "test", template = "{{ \"MSG_HELLO\" | translate }} {{ \"MSG_QBERT\" | translate:\"cool\" }}")
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

	//

	@Component(selector = "test", template = "{{ null | translate }}")
	@NgModule(imports = { TranslateModule.class })
	public static class NullCmp {
	}

	@Test
	public void testNull() {
		assertThat(render(NullCmp.class)).isEqualTo("");
	}
}
