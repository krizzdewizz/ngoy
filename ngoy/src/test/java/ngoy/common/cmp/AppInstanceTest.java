package ngoy.common.cmp;

import static ngoy.core.Provider.useValue;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

import ngoy.ANgoyTest;
import ngoy.core.Component;
import ngoy.core.Inject;
import ngoy.core.NgoyException;
import ngoy.service.OkService;

public class AppInstanceTest extends ANgoyTest {

	@Component(selector = "test", template = "{{msg}}{{okService.ok}}")
	public static class Cmp {
		public Cmp() {
			throw new NgoyException("must not be called");
		}

		@Inject
		public OkService okService;

		public String msg;

		public Cmp(String msg) {
			this.msg = msg;
		}
	}

	@Test
	public void testExistingApp() {
		Cmp cmp = new Cmp("hello");
		assertThat(render(Cmp.class, useValue(Cmp.class, cmp), useValue(OkService.class, OkService.OK))).isEqualTo("hellotrue");
	}
}
