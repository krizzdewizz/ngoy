package ngoy.common.cmp;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

import ngoy.ANgoyTest;
import ngoy.core.Component;

public class TextBindingTest extends ANgoyTest {

	@Component(selector = "test", template = "hello{{aNullValue}} {{'world'}} {{'x'}} {{0}} {{true}} <span>{{'s'}}</span> ")
	public static class Text {
		public String aNullValue = null;
	}

	@Test
	public void testText() {
		assertThat(render(Text.class)).isEqualTo("hello world x 0 true <span>s</span> ");
	}

	//

	@Test
	public void testTextNull() {
		assertThat(render("hello {{'world'}} {{'x'}} {{0}} {{true}} <span>{{'s'}}</span> ")).isEqualTo("hello world x 0 true <span>s</span> ");
	}
}
