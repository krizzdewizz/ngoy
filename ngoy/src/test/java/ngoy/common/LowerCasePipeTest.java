package ngoy.common;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

import ngoy.ANgoyTest;

public class LowerCasePipeTest extends ANgoyTest {
	@Test
	public void test() {
		assertThat(render("{{ 'HALLÖCHEN' | lowercase }}")).isEqualTo("hallöchen");
	}

	@Test
	public void testNull() {
		assertThat(render("{{ null | lowercase }}")).isEqualTo("");
	}
}
