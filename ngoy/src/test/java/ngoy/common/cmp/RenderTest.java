package ngoy.common.cmp;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

import ngoy.ANgoyTest;
import ngoy.core.Component;
import ngoy.core.OnRender;
import ngoy.core.Output;

public class RenderTest extends ANgoyTest {

	@Component(selector = "", template = "abc")
	public static class TestCmp implements OnRender {

		@Override
		public void ngOnRender(Output output) {
			output.write("äöü");
		}

		@Override
		public void ngOnRenderEnd(Output output) {
			output.write("the end");
		}
	}

	@Test
	public void test() {
		assertThat(render(TestCmp.class)).isEqualTo("äöüabcthe end");
	}
}
