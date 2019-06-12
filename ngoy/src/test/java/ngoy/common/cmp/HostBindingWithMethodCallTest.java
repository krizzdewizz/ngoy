package ngoy.common.cmp;

import ngoy.ANgoyTest;
import ngoy.core.Component;
import ngoy.core.HostBinding;
import ngoy.core.Inject;
import ngoy.core.NgModule;
import ngoy.service.OkService;
import org.junit.Test;

import static java.lang.String.format;
import static ngoy.core.Provider.useValue;
import static org.assertj.core.api.Assertions.assertThat;

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

	@Component(selector = "test", template = "<person></person>")
	@NgModule(declarations = { PersonCmp.class })
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
