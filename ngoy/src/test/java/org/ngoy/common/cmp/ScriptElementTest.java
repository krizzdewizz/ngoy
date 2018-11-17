package org.ngoy.common.cmp;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;
import org.ngoy.ANgoyTest;
import org.ngoy.core.Component;

public class ScriptElementTest extends ANgoyTest {

	@Component(selector = "test", template = "<script> alert('hello'); </script>")
	public static class CmpIf {
	}

	@Test
	public void testIf() {
		assertThat(render(CmpIf.class)).isEqualTo("<script> alert('hello'); </script>");
	}
}
