package ngoy.common.cmp;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import ngoy.ANgoyTest;
import ngoy.core.Component;

public class StylesTest extends ANgoyTest {

	@Component(selector = "test", template = "<a style=\"white-space:nowrap\" [style.qbert]=\"notAString\" [style.nully]=\"nully\" [style.empty]=\"empty\" [style.color]=\"col\" [style.width.px]=\"w\" [ngStyle]=\"{a:w}\"></a>")
	public static class Cmp {
		public String col = "red";
		public int w = 10;
		public String empty = "";
		public String nully = null;
		public int notAString = 22;
	}

	@Test
	public void testCmp() {
		assertThat(render(Cmp.class)).isEqualTo("<a style=\"white-space:nowrap;qbert:22;color:red;width:10px;a:10\"></a>");
	}

	//

	@Component(selector = "test", template = "<a style=\"white-space:nowrap\" [style.color]=\"col\" [style.width.px]=\"w\" [ngStyle]=\"stylez\"></a>")
	public static class CmpExpr {

		public Map<String, String> stylez;

		public CmpExpr() {
			stylez = new HashMap<>();
			stylez.put("font-weight", "bold");
		}

		public String col = "red";
		public int w = 10;

	}

	@Test
	public void testCmpExpr() {
		assertThat(render(CmpExpr.class)).isEqualTo("<a style=\"white-space:nowrap;color:red;width:10px;font-weight:bold\"></a>");
	}

}
