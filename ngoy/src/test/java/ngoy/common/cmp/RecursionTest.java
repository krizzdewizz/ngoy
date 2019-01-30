package ngoy.common.cmp;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

import ngoy.ANgoyTest;
import ngoy.core.Component;
import ngoy.core.Input;
import ngoy.core.NgModule;

public class RecursionTest extends ANgoyTest {

	@Component(selector = "x", template = "x:{{title}}<x *ngIf=\"hasChild\"></x>")
	public static class XCmp {

		@Input
		public String title;

		@Input
		public boolean hasChild;
	}

	@Component(selector = "", template = "<x title=\"root\" [hasChild]=\"true\"></x>")
	@NgModule(declarations = { XCmp.class })
	public static class TestCmp {
	}

	@Test
	public void test() {
		assertThat(render(TestCmp.class)).isEqualTo("<x>x:root<x>x:</x></x>");
	}
}
