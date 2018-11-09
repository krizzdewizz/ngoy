package org.ngoy.common.directive;

import static org.assertj.core.api.Assertions.assertThat;
import static org.ngoy.core.Provider.useValue;

import org.junit.Test;
import org.ngoy.ANgoyTest;
import org.ngoy.core.Component;
import org.ngoy.core.Directive;
import org.ngoy.core.HostBinding;
import org.ngoy.core.Inject;
import org.ngoy.core.Input;
import org.ngoy.service.OkService;

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

	@Component(selector = "test", declarations = { SomeDirective.class }, template = "<a [upper]=\"ok\" [makeIt]=\"'bi'\">XX</a>")
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
