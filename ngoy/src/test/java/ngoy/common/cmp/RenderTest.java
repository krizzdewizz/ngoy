package ngoy.common.cmp;

import ngoy.ANgoyTest;
import ngoy.core.Component;
import ngoy.core.OnRender;
import ngoy.core.Output;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class RenderTest extends ANgoyTest {

	@Component(selector = "", template = "abc")
	public static class TestCmp implements OnRender {

		@Override
		public void onRender(Output output) {
			output.write("äöü");
		}

		@Override
		public void onRenderEnd(Output output) {
			output.write("the end");
		}
	}

	@Test
	public void test() {
		assertThat(render(TestCmp.class)).isEqualTo("äöüabcthe end");
	}
}
