package org.ngoy.common.cmp;

import static java.lang.String.format;
import static org.assertj.core.api.Assertions.assertThat;
import static org.ngoy.core.Provider.useValue;

import org.junit.Test;
import org.ngoy.ANgoyTest;
import org.ngoy.core.Component;
import org.ngoy.core.HostBinding;
import org.ngoy.core.Inject;
import org.ngoy.service.OkService;

public class HostBindingWithMethodCallTest extends ANgoyTest {

	@Component(selector = "person", template = "")
	public static class PersonCmp {
		@Inject
		public OkService okService;

		@HostBinding("class.x")
		public boolean isGood() {
			return okService.ok;
		}

		@HostBinding("attr.href")
		public String href() {
			return format("http://%s", okService.ok);
		}
	}

	@Component(selector = "test", declarations = { PersonCmp.class }, template = "<person></person>")
	public static class Cmp {
	}

	@Test
	public void testOk() {
		assertThat(render(Cmp.class, useValue(OkService.class, OkService.OK))).isEqualTo("<person class=\"x\" href=\"http://true\"></person>");
	}

	@Test
	public void testNok() {
		assertThat(render(Cmp.class, useValue(OkService.class, OkService.NOK))).isEqualTo("<person href=\"http://false\"></person>");
	}
}
