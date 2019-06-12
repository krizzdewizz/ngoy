package ngoy.common.cmp;

import ngoy.ANgoyTest;
import ngoy.core.Component;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class ScriptElementTest extends ANgoyTest {

	@Component(selector = "", template = "<script> alert('hello'); </script>")
	public static class TestCmp {
	}

	@Test
	public void test() {
		assertThat(render(TestCmp.class)).isEqualTo("<script> alert('hello'); </script>");
	}
}
