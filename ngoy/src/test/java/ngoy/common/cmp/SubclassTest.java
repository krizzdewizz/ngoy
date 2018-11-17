package ngoy.common.cmp;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

import ngoy.ANgoyTest;
import ngoy.core.Component;
import ngoy.core.Input;
import ngoy.core.NgModule;

public class SubclassTest extends ANgoyTest {

	public static abstract class ACmpBase {
		@Input
		public String name;
	}

	@Component(selector = "person", template = "hello:{{name}}")
	public static class PersonCmp extends ACmpBase {
	}

	@Component(selector = "test", template = "<person [name]=\"'a'\"></person><person [name]=\"'b'\"></person>")
	@NgModule(declarations = { PersonCmp.class })
	public static class Cmp extends ACmpBase {
	}

	@Test
	public void testAttr() {
		assertThat(render(Cmp.class)).isEqualTo("<person>hello:a</person><person>hello:b</person>");
	}
}
