package ngoy.common.cmp;

import ngoy.ANgoyTest;
import ngoy.core.Component;
import ngoy.core.HostBinding;
import ngoy.core.NgModule;
import org.junit.Test;

import java.util.Map;

import static hyperml.base.BaseMl.map;
import static hyperml.base.HtmlCore.color;
import static hyperml.base.HtmlCore.display;
import static org.assertj.core.api.Assertions.assertThat;

public class HostBindingNgStyleTest extends ANgoyTest {
	@Component(selector = "x", template = "hello")
	public static class XCmp {

		@HostBinding("ngStyle")
		public Map<String, String> styles = map(color, "red", display, null);
	}

	@Component(selector = "", template = "<x style=\"background-color:green\"></x>")
	@NgModule(declarations = { XCmp.class })
	public static class TestAppCmp {
	}

	@Test
	public void testApp() {
		assertThat(render(TestAppCmp.class)).isEqualTo("<x style=\"background-color:green;color:red\">hello</x>");
	}
}
