package ngoy.common.cmp;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

import ngoy.ANgoyTest;
import ngoy.core.Component;
import ngoy.core.NgModule;
import ngoy.translate.TranslateModule;

public class CmpVerifyErrorTest extends ANgoyTest {

	@Component(selector = "person", template = "<h2 translate=\"MSG_WELCOME\"></h2>")
	public static class PersonCmp {
	}

	@Component(selector = "test", template = "<person *ngIf=\"true\"></person><h2 translate=\"MSG_WELCOME\"></h2>")
	@NgModule(declarations = { PersonCmp.class }, imports = { TranslateModule.class })
	public static class Cmp {
	}

	@Test
	public void test() {
		assertThat(render(Cmp.class)).isEqualTo("<person><h2>MSG_WELCOME</h2></person><h2>MSG_WELCOME</h2>");
	}
}
