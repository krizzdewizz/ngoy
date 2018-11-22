package ngoy.common;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

import ngoy.ANgoyTest;
import ngoy.core.Component;

public class UpperCasePipeTest extends ANgoyTest {

	@Component(selector = "test", template = "<a [q]=\"'a' | uppercase\">{{ 'HALLÖCHEN' | uppercase }}</a>")
	public static class Cmp {
	}

	@Test
	public void test() {
		assertThat(render(Cmp.class)).isEqualTo("<a q=\"A\">HALLÖCHEN</a>");
	}
}
