package ngoy.hyperml.cmp;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

import ngoy.ANgoyTest;
import ngoy.core.Component;
import ngoy.core.HostBinding;
import ngoy.core.NgModule;
import ngoy.hyperml.HtmlComponent;

public class MixedTest extends ANgoyTest {

	@Component(selector = "y", template = "helloy")
	public static class YCmp {
		@HostBinding("attr.title")
		public String title = "hello";
	}

	@Component(selector = "x")
	public static class XCmp extends HtmlComponent {

		@Override
		protected void content() {
			text("hellox:");
			$("y", $);
		}
	}

	@Component(selector = "", template = "<x></x>")
	@NgModule(declarations = { XCmp.class, YCmp.class })
	public static class TestAppCmp {
	}

	@Test
	public void testMixed() {
		assertThat(render(TestAppCmp.class)).isEqualTo("<x>hellox:<y title=\"hello\">helloy</y></x>");
	}
}
