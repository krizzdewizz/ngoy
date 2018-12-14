package ngoy.common;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

import ngoy.ANgoyTest;
import ngoy.core.Component;

public class LowerCasePipeTest extends ANgoyTest {
	@Component(selector = "", template = "{{ \"HALLÖCHEN\" | lowercase }}")
	public static class LowerCmp {
	}

	@Test
	public void test() {
		assertThat(render(LowerCmp.class)).isEqualTo("hallöchen");
	}

	//

	@Component(selector = "", template = "{{ null | lowercase }}")
	public static class NullCmp {
	}

	@Test
	public void testNull() {
		assertThat(render(NullCmp.class)).isEqualTo("");
	}
}
