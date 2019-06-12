package ngoy.common.cmp;

import ngoy.ANgoyTest;
import ngoy.core.Component;
import ngoy.core.NgModule;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class DeepDeclTest extends ANgoyTest {

	@Component(selector = "a", template = "a")
	public static class CmpA {
	}

	@Component(selector = "b", template = "b<a></a>")
	public static class CmpB {
	}

	@Component(selector = "test", template = "hello<b></b>")
	@NgModule(declarations = { CmpA.class, CmpB.class })
	public static class Cmp {
	}

	@Test
	public void test() {
		assertThat(render(Cmp.class)).isEqualTo("hello<b>b<a>a</a></b>");
	}
}
