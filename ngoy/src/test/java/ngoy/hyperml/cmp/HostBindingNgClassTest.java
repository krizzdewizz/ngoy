package ngoy.hyperml.cmp;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;

import org.junit.Test;

import ngoy.ANgoyTest;
import ngoy.core.Component;
import ngoy.core.HostBinding;
import ngoy.core.NgModule;
import ngoy.hyperml.HtmlComponent;

public class HostBindingNgClassTest extends ANgoyTest {
	@Component(selector = "x")
	public static class XCmp extends HtmlComponent {

		@HostBinding("ngClass")
		public Map<String, Boolean> classes = map("xtitle", Boolean.TRUE, "cool", true, "not-there", false, "not-there-null", null);

		@Override
		protected void content() {
			text("hello");
		}
	}

	@Component(selector = "")
	@NgModule(declarations = { XCmp.class })
	public static class TestAppCmp extends HtmlComponent {

		@Override
		protected void content() {
			$("x", classs, "honk xs-9", $);
		}
	}

	@Test
	public void testApp() {
		assertThat(render(TestAppCmp.class)).isEqualTo("<x class=\"honk xs-9 xtitle cool\">hello</x>");
	}
}
