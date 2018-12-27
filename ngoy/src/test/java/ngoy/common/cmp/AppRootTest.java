package ngoy.common.cmp;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

import ngoy.ANgoyTest;
import ngoy.core.AppRoot;
import ngoy.core.Component;
import ngoy.core.Inject;
import ngoy.core.NgModule;

public class AppRootTest extends ANgoyTest {

	@Component(selector = "nested", template = "{{ appRoot.appClass.simpleName }}")
	public static class NestedCmp {

		@Inject
		public AppRoot appRoot;
	}

	@Component(selector = "", template = "<nested></nested>")
	@NgModule(declarations = { NestedCmp.class })
	public static class TestCmp {
	}

	@Test
	public void test() {
		assertThat(render(TestCmp.class)).isEqualTo("<nested>TestCmp</nested>");
	}
}
