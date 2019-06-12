package ngoy.hyperml.cmp;

import ngoy.ANgoyTest;
import ngoy.core.Component;
import ngoy.core.HostBinding;
import ngoy.core.NgModule;
import ngoy.hyperml.HtmlComponent;
import org.junit.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class HostBindingNgStyleTest extends ANgoyTest {
	@Component(selector = "x")
	public static class XCmp extends HtmlComponent {

		@HostBinding("ngStyle")
		public Map<String, String> styles = map(color, "red", display, null);

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
			$("x", style, "background-color:green", $);
		}
	}

	@Test
	public void testApp() {
		assertThat(render(TestAppCmp.class)).isEqualTo("<x style=\"background-color:green;color:red\">hello</x>");
	}
}
