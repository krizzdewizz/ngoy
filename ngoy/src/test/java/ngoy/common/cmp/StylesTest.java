package ngoy.common.cmp;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

import ngoy.ANgoyTest;
import ngoy.core.Component;

public class StylesTest extends ANgoyTest {

	@Component(selector = "test", template = "<a style=\"white-space:nowrap\" [style.color]=\"col\" [style.width.px]=\"w\" [ngStyle]=\"{a:'abc'}\"></a>")
	public static class Cmp {
		public String col = "red";
		public int w = 10;
	}

	@Test
	public void testCmp() {
		assertThat(render(Cmp.class)).isEqualTo("<a style=\"white-space:nowrap;color:red;width:10px;a:abc\"></a>");
	}
}
