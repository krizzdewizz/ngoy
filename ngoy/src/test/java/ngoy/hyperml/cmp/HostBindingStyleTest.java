package ngoy.hyperml.cmp;

import ngoy.ANgoyTest;
import ngoy.core.Component;
import ngoy.core.HostBinding;
import ngoy.core.NgModule;
import ngoy.core.OnInit;
import ngoy.hyperml.HtmlComponent;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class HostBindingStyleTest extends ANgoyTest {
	@Component(selector = "x")
	public static class XCmp extends HtmlComponent implements OnInit {

		@HostBinding("style.color")
		public String color;

		@HostBinding("style.display")
		public String getDisplay() {
			return "grid";
		}

		@HostBinding("style.none")
		public Object getNone() {
			return null;
		}

		@Override
		protected void content() {
			text("hello");
		}

		@Override
		public void onInit() {
			color = "red";
		}
	}

	@Component(selector = "")
	@NgModule(declarations = { XCmp.class })
	public static class TestAppCmp extends HtmlComponent {

		@Override
		protected void content() {
			$("x", style, "white-space: nowrap; border:none", $);
		}
	}

	@Test
	public void testApp() {
		assertThat(render(TestAppCmp.class)).isEqualTo("<x style=\"white-space: nowrap; border:none;color:red;display:grid\">hello</x>");
	}
}
