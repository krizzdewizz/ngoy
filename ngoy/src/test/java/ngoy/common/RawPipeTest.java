package ngoy.common;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

import ngoy.ANgoyTest;
import ngoy.core.Component;

public class RawPipeTest extends ANgoyTest {
	@Component(selector = "a-cmp", template = "{{ rawText | raw  }}")
	public static class RawCmp {
		public String rawText = "<>";
	}

	@Test
	public void testRaw() {
		assertThat(render(RawCmp.class)).isEqualTo("<>");
	}
}
