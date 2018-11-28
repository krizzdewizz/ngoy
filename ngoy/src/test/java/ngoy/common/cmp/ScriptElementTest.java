package ngoy.common.cmp;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

import ngoy.ANgoyTest;

public class ScriptElementTest extends ANgoyTest {
	@Test
	public void testIf() {
		assertThat(render("<script> alert('hello'); </script>")).isEqualTo("<script> alert('hello'); </script>");
	}
}
