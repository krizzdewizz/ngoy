package ngoy.common.directive;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

import jodd.jerry.Jerry;
import ngoy.ANgoyTest;
import ngoy.core.Component;
import ngoy.core.Directive;
import ngoy.core.HostBinding;
import ngoy.core.NgModule;
import ngoy.core.OnCompile;

public class DirectiveTest extends ANgoyTest {

	@Directive(selector = "[makeBold]")
	public static class MakeBoldCompileDirective implements OnCompile {
		@Override
		public void ngOnCompile(Jerry el, String componentClass) {
			el.attr("style", "font-weight:bold");
		}
	}

	@Component(selector = "test", template = "<a makeBold>XX</a>")
	@NgModule(declarations = { MakeBoldCompileDirective.class })
	public static class Cmp {
	}

	@Test
	public void testCompile() {
		assertThat(render(Cmp.class)).isEqualTo("<a makeBold style=\"font-weight:bold\">XX</a>");
	}

	//

	@Directive(selector = "[addBold]")
	public static class AddBoldDirective {
		@HostBinding("class.bold")
		public boolean ok = true;
	}

	@Directive(selector = "[addHref]")
	public static class AddHRefAttributeDirective {
		@HostBinding("attr.href")
		public String href = "http://x";
	}

	@Component(selector = "test", template = "<a addBold addHref>XX</a>")
	@NgModule(declarations = { AddBoldDirective.class, AddHRefAttributeDirective.class })
	public static class CmpHostBinding {
	}

	@Test
	public void testHostBinding() {
		assertThat(render(CmpHostBinding.class)).isEqualTo("<a addBold addHref class=\"bold\" href=\"http://x\">XX</a>");
	}

}
