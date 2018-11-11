package org.ngoy.common.cmp;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;
import org.ngoy.ANgoyTest;
import org.ngoy.core.Component;
import org.ngoy.core.NgModule;

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
