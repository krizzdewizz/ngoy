package ngoy.common.cmp;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

import ngoy.ANgoyTest;

public class TextOnlyTest extends ANgoyTest {

	@Test
	public void test() {
		assertThat(render("hello")).isEqualTo("hello");
	}
}
