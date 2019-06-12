package ngoy.common;

import ngoy.ANgoyTest;
import ngoy.core.Component;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

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
