package ngoy.hyperml.cmp;

import ngoy.ANgoyTest;
import ngoy.core.Component;
import ngoy.core.HostBinding;
import ngoy.core.NgModule;
import ngoy.core.OnInit;
import ngoy.hyperml.HtmlComponent;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class HostBindingClassTest extends ANgoyTest {
	@Component(selector = "x")
	public static class XCmp extends HtmlComponent implements OnInit {

		@HostBinding("class.xtitle")
		public boolean title;

		@HostBinding("class.cool")
		public boolean getCool() {
			return true;
		}

		@HostBinding("class.none")
		public boolean getNone() {
			return false;
		}

		@Override
		protected void content() {
			text("hello");
		}

		@Override
		public void onInit() {
			title = true;
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
