package ngoy.common.directive;

import jodd.jerry.Jerry;
import ngoy.ANgoyTest;
import ngoy.core.Component;
import ngoy.core.Directive;
import ngoy.core.NgModule;
import ngoy.core.OnCompile;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class DirectiveMany2Test extends ANgoyTest {

	@Directive(selector = "html")
	public static class ADirective implements OnCompile {

		@Override
		public void onCompile(Jerry el, String componentClass) {
			el.attr("a-attr", "a");
		}
	}

	@Directive(selector = "html")
	public static class BDirective implements OnCompile {

		@Override
		public void onCompile(Jerry el, String componentClass) {
			el.attr("b-attr", "b");
		}
	}

	@Component(selector = "test", template = "<html>XX</html>")
	@NgModule(declarations = { ADirective.class, BDirective.class })
	public static class Cmp {
	}

	@Test
	public void test() {
		assertThat(render(Cmp.class)).isEqualTo("<html a-attr=\"a\" b-attr=\"b\">XX</html>");
	}
}
