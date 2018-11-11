package org.ngoy.common.directive;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;
import org.ngoy.ANgoyTest;
import org.ngoy.core.Component;
import org.ngoy.core.Directive;
import org.ngoy.core.HostBinding;
import org.ngoy.core.Input;
import org.ngoy.core.NgModule;

public class DirectiveAndCmpTest extends ANgoyTest {

	@Directive(selector = "[makeItBold]")
	public static class BoldDirective {
		@Input
		@HostBinding("attr.q-bold")
		public String makeItBold;
	}

	@Component(selector = "a", template = "hello:{{q}}")
	public static class ACmp {
		public String q = "abc";
	}

	@Component(selector = "test", template = "<a [attr.q]=\"q\" [makeItBold]=\"'bi'\"></a>")
	@NgModule(declarations = { ACmp.class, BoldDirective.class })
	public static class Cmp {
		public String q = "abc";
	}

	@Test
	public void test() {
		assertThat(render(Cmp.class)).isEqualTo("<a q=\"abc\" q-bold=\"bi\">hello:abc</a>");
	}
}
