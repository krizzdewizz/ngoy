package ngoy.common.cmp;

import ngoy.ANgoyTest;
import ngoy.core.Component;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class TextOnlyTest extends ANgoyTest {

	@Component(selector = "", template = "hello")
	public static class TestCmp {
	}

	@Test
	public void test() {
		assertThat(render(TestCmp.class)).isEqualTo("hello");
	}
}
