package ngoy.common.cmp;

import static ngoy.core.Provider.useValue;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

import ngoy.ANgoyTest;
import ngoy.core.Component;
import ngoy.core.HostBinding;
import ngoy.core.Inject;
import ngoy.core.NgModule;
import ngoy.core.OnInit;
import ngoy.service.OkService;

public class HostBindingClassTest extends ANgoyTest {

	@Component(selector = "person", template = "")
	public static class PersonCmp implements OnInit {
		@Inject
		public OkService okService;

		@HostBinding("class.x")
		public boolean ok;

		@Override
		public void onInit() {
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
