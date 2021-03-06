package ngoy.hyperml.cmp;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

import ngoy.ANgoyTest;
import ngoy.core.Component;
import ngoy.core.HostBinding;
import ngoy.core.NgModule;
import ngoy.core.OnInit;
import ngoy.hyperml.HtmlComponent;

public class HostBindingAttrTest extends ANgoyTest {
	@Component(selector = "x")
	public static class XCmp extends HtmlComponent implements OnInit {

		@HostBinding("attr.xtitle")
		public String title;

		@HostBinding("attr.xage")
		public int getAge() {
			return 21;
		}

		@Override
		protected void content() {
			text("hello");
		}

		@Override
		public void onInit() {
			title = "hello-title";
		}
	}

	@Component(selector = "")
	@NgModule(declarations = { XCmp.class })
	public static class TestAppCmp extends HtmlComponent {

		@Override
		protected void content() {
			$("x", $);
		}
	}

	@Test
	public void testApp() {
		assertThat(render(TestAppCmp.class)).isEqualTo("<x xtitle=\"hello-title\" xage=\"21\">hello</x>");
	}
}
