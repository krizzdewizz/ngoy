package org.ngoy.common.directive;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;
import org.ngoy.ANgoyTest;
import org.ngoy.core.Component;
import org.ngoy.core.Directive;
import org.ngoy.core.HostBinding;
import org.ngoy.core.Input;
import org.ngoy.core.NgModule;

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

	@Component(selector = "test", template = "<a [makeItBold]=\"'bi'\" [makeItItalic]=\"'yes'\">XX</a>")
	@NgModule(declarations = { BoldDirective.class, ItalicDirective.class })
	public static class Cmp {
	}

	@Test
	public void test() {
		assertThat(render(Cmp.class)).isEqualTo("<a q-bold=\"bi\" q-italic=\"yes\">XX</a>");
	}
}
