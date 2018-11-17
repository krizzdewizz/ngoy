package ngoy.translate;

import static ngoy.core.Provider.useValue;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.Locale;

import org.junit.Test;

import ngoy.ANgoyTest;
import ngoy.core.Component;
import ngoy.core.LocaleProvider;
import ngoy.core.NgModule;
import ngoy.translate.TranslateModule;
import ngoy.translate.TranslateService;

public class TranslateDirectiveTest extends ANgoyTest {

	@Component(selector = "test", template = "<a translate=\"MSG_HELLO\">x</a><a [translate]=\"msg\">x</a>")
	@NgModule(imports = { TranslateModule.class })
	public static class Cmp {

		public String msg = "MSG_HELLO";

		public Cmp(TranslateService service) {
			service.setBundle(TranslateDirectiveTest.class.getPackage()
					.getName() + ".messages");
		}
	}

	@Test
	public void test() {
		assertThat(render(Cmp.class, useValue(LocaleProvider.class, new LocaleProvider.Default(Locale.GERMAN)))).isEqualTo("<a>hallo</a><a>hallo</a>");
	}
}
