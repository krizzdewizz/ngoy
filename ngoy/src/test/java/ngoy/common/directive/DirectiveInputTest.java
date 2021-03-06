package ngoy.common.directive;

import static ngoy.core.Provider.useValue;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

import ngoy.ANgoyTest;
import ngoy.core.Component;
import ngoy.core.Directive;
import ngoy.core.HostBinding;
import ngoy.core.Inject;
import ngoy.core.Input;
import ngoy.core.NgModule;
import ngoy.service.OkService;

public class DirectiveInputTest extends ANgoyTest {

	@Directive(selector = "[makeIt]")
	public static class SomeDirective {
		@Input
		public void makeIt(String value) {
			this.bold = value.indexOf('b') > -1;
			this.italic = value.indexOf('i') > -1;
		}

		@HostBinding("class.q-bold")
		public boolean bold = false;

		@HostBinding("class.q-italic")
		public boolean italic = false;

		@HostBinding("class.q-upper")
		@Input
		public boolean upper = false;
	}

	@Component(selector = "test", template = "<a [upper]='isOk()' [makeIt]='\"bi\"'>XX</a>")
	@NgModule(declarations = { SomeDirective.class })
	public static class Cmp {
		@Inject
		public OkService okService;

		public boolean isOk() {
			return okService.ok;
		}
	}

	@Test
	public void test() {
		assertThat(render(Cmp.class, useValue(OkService.class, OkService.NOK))).isEqualTo("<a class=\"q-bold q-italic\">XX</a>");
	}

	@Test
	public void testUpper() {
		assertThat(render(Cmp.class, useValue(OkService.class, OkService.OK))).isEqualTo("<a class=\"q-bold q-italic q-upper\">XX</a>");
	}

}
