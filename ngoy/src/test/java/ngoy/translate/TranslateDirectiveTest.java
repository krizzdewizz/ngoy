package ngoy.translate;

import static ngoy.core.Provider.useValue;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.Locale;

import org.junit.Test;

import ngoy.ANgoyTest;
import ngoy.core.Component;
import ngoy.core.LocaleProvider;

public class TranslateDirectiveTest extends ANgoyTest {

	@Component(selector = "test", template = "<a translate=\"MSG_HELLO\">x</a><a [translate]=\"msg\">x</a>")
	public static class Cmp {
		public String msg = "MSG_HELLO";
	}

	@Test
	public void test() {
		assertThat(render(Cmp.class, builder -> builder.translateBundle(TranslateDirectiveTest.class.getPackage()
				.getName() + ".messages"), useValue(LocaleProvider.class, new LocaleProvider.Default(Locale.GERMAN)))).isEqualTo("<a>hallo</a><a>hallo</a>");
	}
}
