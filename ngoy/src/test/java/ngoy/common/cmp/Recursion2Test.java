package ngoy.common.cmp;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

import ngoy.ANgoyTest;
import ngoy.core.Component;
import ngoy.core.Input;
import ngoy.core.NgModule;

public class Recursion2Test extends ANgoyTest {

	@Component(selector = "y", template = "YYYY")
	public static class YCmp {
	}

	@Component(selector = "x", template = "x:{{title}}<y *ngIf=\"hasChild\"></y>")
	public static class XCmp {

		@Input
		public String title;

		@Input
		public boolean hasChild;
	}

	@Component(selector = "", template = "<x [hasChild]=\"true\"></x>")
	@NgModule(declarations = { XCmp.class, YCmp.class })
	public static class TestCmp {
	}

	@Test
	public void test() {
		assertThat(render(TestCmp.class)).isEqualTo("<x>x:<y>YYYY</y></x>");
	}
}
