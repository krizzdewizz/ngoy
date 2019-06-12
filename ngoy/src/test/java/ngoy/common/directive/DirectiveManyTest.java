package ngoy.common.directive;

import ngoy.ANgoyTest;
import ngoy.core.Component;
import ngoy.core.Directive;
import ngoy.core.HostBinding;
import ngoy.core.Input;
import ngoy.core.NgModule;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class DirectiveManyTest extends ANgoyTest {

	@Directive(selector = "[makeItBold]")
	public static class BoldDirective {
		@Input
		@HostBinding("attr.q-bold")
		public String makeItBold;
	}

	@Directive(selector = "[makeItItalic]")
	public static class ItalicDirective {
		@Input
		@HostBinding("attr.q-italic")
		public String makeItItalic;
	}

	@Component(selector = "test", template = "<a [makeItBold]='\"bi\"' [makeItItalic]='\"yes\"'>XX</a>")
	@NgModule(declarations = { BoldDirective.class, ItalicDirective.class })
	public static class Cmp {
	}

	@Test
	public void test() {
		assertThat(render(Cmp.class)).isEqualTo("<a q-bold=\"bi\" q-italic=\"yes\">XX</a>");
	}
}
