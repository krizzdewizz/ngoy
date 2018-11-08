package org.ngoy.common.directive;

import static org.assertj.core.api.Assertions.assertThat;

import org.jsoup.nodes.Element;
import org.junit.Test;
import org.ngoy.common.ANgoyTest;
import org.ngoy.core.Component;
import org.ngoy.core.Directive;
import org.ngoy.core.ElementRef;
import org.ngoy.core.HostBinding;
import org.ngoy.core.OnCompile;

public class DirectiveTest extends ANgoyTest {

	@Directive(selector = "[makeBold]")
	public static class MakeBoldCompileDirective implements OnCompile {
		@Override
		public void ngOnCompile(ElementRef elRef) {
			((Element) elRef.getNativeElement()).attr("style", "font-weight:bold");
		}
	}

	@Component(selector = "test", declarations = { MakeBoldCompileDirective.class }, template = "<a makeBold>XX</a>")
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

	@Component(selector = "test", declarations = { AddBoldDirective.class, AddHRefAttributeDirective.class }, template = "<a addBold addHref>XX</a>")
	public static class CmpHostBinding {
	}

	@Test
	public void testHostBinding() {
		assertThat(render(CmpHostBinding.class)).isEqualTo("<a addbold addhref href=\"http://x\" class=\"bold\">XX</a>");
	}

}
