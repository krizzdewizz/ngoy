package org.ngoy.common.directive;

import static org.assertj.core.api.Assertions.assertThat;

import org.jsoup.nodes.Element;
import org.junit.Test;
import org.ngoy.ANgoyTest;
import org.ngoy.core.Component;
import org.ngoy.core.Directive;
import org.ngoy.core.HostBinding;
import org.ngoy.core.NgModule;
import org.ngoy.core.OnCompile;

public class DirectiveTest extends ANgoyTest {

	@Directive(selector = "[makeBold]")
	public static class MakeBoldCompileDirective implements OnCompile {
		@Override
		public void ngOnCompile(Element el, String cmpClass) {
			el.attr("style", "font-weight:bold");
		}
	}

	@Component(selector = "test", template = "<a makeBold>XX</a>")
	@NgModule(declarations = { MakeBoldCompileDirective.class })
	public static class Cmp {
	}

	@Test
	public void testCompile() {
		assertThat(render(Cmp.class)).isEqualTo("<a makebold style=\"font-weight:bold\">XX</a>");
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
		assertThat(render(CmpHostBinding.class)).isEqualTo("<a addbold addhref class=\"bold\" href=\"http://x\">XX</a>");
	}

}
