package ngoy.common.cmp;

import ngoy.ANgoyTest;
import ngoy.core.Component;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class TextBindingTest extends ANgoyTest {

	@Component(selector = "test", template = "hello{{aNullValue}} {{\"world\"}} {{\"x\"}} {{0}} {{true}} <span>{{\"s\"}}</span> ")
	public static class Text {
		public String aNullValue = null;
	}

	@Test
	public void testText() {
		assertThat(render(Text.class)).isEqualTo("hello world x 0 true <span>s</span> ");
	}

	//

	@Component(selector = "", template = "hello {{\"world\"}} {{\"x\"}} {{0}} {{true}} <span>{{\"s\"}}</span> ")
	public static class NullCmp {
	}

	@Test
	public void testTextNull() {
		assertThat(render(NullCmp.class)).isEqualTo("hello world x 0 true <span>s</span> ");
	}
}
