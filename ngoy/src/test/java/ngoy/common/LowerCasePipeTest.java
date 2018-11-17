package ngoy.common;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

import ngoy.ANgoyTest;
import ngoy.core.Component;

public class LowerCasePipeTest extends ANgoyTest {

	@Component(selector = "test", template = "{{ 'HALLÖCHEN' | lowercase }}")
	public static class Cmp {
	}

	@Test
	public void test() {
		assertThat(render(Cmp.class)).isEqualTo("hall&ouml;chen");
	}
}
