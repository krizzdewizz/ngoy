package ngoy.forms;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

import ngoy.ANgoyTest;
import ngoy.core.Component;
import ngoy.core.NgModule;

public class ButtonClickDirectiveTest extends ANgoyTest {

	@Component(selector = "person", template = "save me: <button class=\"x\" x=\"y\" (click)=\"/saveme\"></button>")
	@NgModule(declarations = { ButtonClickDirective.class })
	public static class Cmp {
	}

	@Test
	public void test() {
		assertThat(render(Cmp.class)).isEqualTo("save me: <form method=\"POST\" action=\"/saveme\"><button x=\"y\" type=\"submit\" class=\"x\"></button></form>");
	}

	//

	@Component(selector = "person", template = "save me: <button class=\"x\" x=\"y\" [(click)]=\"saveUrl\"></button>")
	@NgModule(declarations = { ButtonClickDirective.class })
	public static class BindingCmp {
		public String saveUrl = "/saveme";
	}

	@Test
	public void testBinding() {
		assertThat(render(BindingCmp.class)).isEqualTo("save me: <form method=\"POST\" action=\"/saveme\"><button x=\"y\" type=\"submit\" class=\"x\"></button></form>");
	}

}
