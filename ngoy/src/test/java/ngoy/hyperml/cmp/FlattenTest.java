package ngoy.hyperml.cmp;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

import ngoy.ANgoyTest;
import ngoy.core.Component;
import ngoy.hyperml.HtmlComponent;

public class FlattenTest extends ANgoyTest {
	@Component(selector = "")
	public static class TestAppCmp extends HtmlComponent {

		@Override
		protected void template() {
			$("x", attr("title", "hello"), list(attr("a", "aval"), attr("b", "bval"), attr("_not_", ""), attr("_not_2_", null)), $);
		}
	}

	@Test
	public void testApp() {
		assertThat(render(TestAppCmp.class)).isEqualTo("<x title=\"hello\" a=\"aval\" b=\"bval\"></x>");
	}
}
