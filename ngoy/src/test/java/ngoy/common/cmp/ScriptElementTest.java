package ngoy.common.cmp;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

import ngoy.ANgoyTest;
import ngoy.core.Component;

public class ScriptElementTest extends ANgoyTest {

	@Component(selector = "", template = "<script> alert('hello'); </script>")
	public static class TestCmp {
	}

	@Test
	public void test() {
		assertThat(render(TestCmp.class)).isEqualTo("<script> alert('hello'); </script>");
	}
}
