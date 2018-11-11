package org.ngoy.common.cmp;

import static org.assertj.core.api.Assertions.assertThat;
import static org.ngoy.core.Provider.useValue;

import org.junit.Test;
import org.ngoy.ANgoyTest;
import org.ngoy.core.Component;
import org.ngoy.core.HostBinding;
import org.ngoy.core.Inject;
import org.ngoy.core.NgModule;
import org.ngoy.core.OnInit;
import org.ngoy.service.OkService;

public class HostBindingClassTest extends ANgoyTest {

	@Component(selector = "person", template = "")
	public static class PersonCmp implements OnInit {
		@Inject
		public OkService okService;

		@HostBinding("class.x")
		public boolean ok;

		@Override
		public void ngOnInit() {
			ok = okService.ok;
		}
	}

	@Component(selector = "test", template = "<person></person>")
	@NgModule(declarations = { PersonCmp.class })
	public static class Cmp {
	}

	@Test
	public void testOk() {
		assertThat(render(Cmp.class, useValue(OkService.class, OkService.OK))).isEqualTo("<person class=\"x\"></person>");
	}

	@Test
	public void testNok() {
		assertThat(render(Cmp.class, useValue(OkService.class, OkService.NOK))).isEqualTo("<person></person>");
	}
}
