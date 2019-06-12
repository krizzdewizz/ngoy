package ngoy.common.cmp;

import ngoy.ANgoyTest;
import ngoy.core.Component;
import ngoy.core.Inject;
import ngoy.service.OkService;
import org.junit.Test;

import static ngoy.core.Provider.useValue;
import static org.assertj.core.api.Assertions.assertThat;

public class ContainerTest extends ANgoyTest {

	@Component(selector = "test", template = "a<ng-container *ngIf=\"isOk()\">x</ng-container>b")
	public static class Cmp {
		@Inject
		public OkService okService;

		public boolean isOk() {
			return okService.ok;
		}
	}

	@Test
	public void test() {
		assertThat(render(Cmp.class, useValue(OkService.class, OkService.OK))).isEqualTo("axb");
	}

	@Test
	public void testNok() {
		assertThat(render(Cmp.class, useValue(OkService.class, OkService.NOK))).isEqualTo("ab");
	}

	//

	@Component(selector = "test", template = "a<ng-container *ngFor=\"let s of strings\">{{s}}</ng-container>b")
	public static class CmpRepeated {
		public String[] strings = new String[] { "w", "x", "q" };
	}

	@Test
	public void testRepeated() {
		assertThat(render(CmpRepeated.class)).isEqualTo("awxqb");
	}
}
