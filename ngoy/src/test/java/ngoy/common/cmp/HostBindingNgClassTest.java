package ngoy.common.cmp;

import static ngoy.core.Util.map;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;

import org.junit.Test;

import ngoy.ANgoyTest;
import ngoy.core.Component;
import ngoy.core.HostBinding;
import ngoy.core.NgModule;

public class HostBindingNgClassTest extends ANgoyTest {
	@Component(selector = "x", template = "hello")
	public static class X1Cmp {
		@HostBinding("class.cool")
		public boolean cool = true;
	}

	@Component(selector = "", template = "<x class=\"q\"></x>")
	@NgModule(declarations = { X1Cmp.class })
	public static class TestApp1Cmp {
	}

	@Test
	public void testApp1() {
		assertThat(render(TestApp1Cmp.class)).isEqualTo("<x class=\"q cool\">hello</x>");
	}

	//

	@Component(selector = "x", template = "hello")
	public static class XCmp {
		@HostBinding("ngClass")
		public Map<String, Boolean> classes = map("xtitle", Boolean.TRUE, "cool", true, "not-there", false, "not-there-null", null);
	}

	@Component(selector = "", template = "<x class=\"q\"></x>")
	@NgModule(declarations = { XCmp.class })
	public static class TestAppCmp {
	}

	@Test
	public void testApp() {
		assertThat(render(TestAppCmp.class)).isEqualTo("<x class=\"q xtitle cool\">hello</x>");
	}
}
