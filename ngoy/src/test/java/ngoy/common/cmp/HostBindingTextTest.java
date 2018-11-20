package ngoy.common.cmp;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

import ngoy.ANgoyTest;
import ngoy.core.Component;
import ngoy.core.HostBinding;
import ngoy.core.NgModule;

public class HostBindingTextTest extends ANgoyTest {

	@Component(selector = "person", template = "")
	public static class PersonCmp {
		@HostBinding("text")
		public String s = "hello";
	}

	@Component(selector = "test", template = "<person>i will be overwritten</person>")
	@NgModule(declarations = { PersonCmp.class })
	public static class Cmp {
	}

	@Test
	public void testHello() {
		assertThat(render(Cmp.class)).isEqualTo("<person>hello</person>");
	}

	//

	@Component(selector = "person", template = "")
	public static class PersonNullCmp {
		@HostBinding("text")
		public String s = null;
	}

	@Component(selector = "test", template = "<person>i will be overwritten</person>")
	@NgModule(declarations = { PersonNullCmp.class })
	public static class NullCmp {
	}

	@Test
	public void testNull() {
		assertThat(render(NullCmp.class)).isEqualTo("<person></person>");
	}
}
