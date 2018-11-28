package ngoy.common;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

import ngoy.ANgoyTest;

public class UpperCasePipeTest extends ANgoyTest {

	@Test
	public void test() {
		assertThat(render("<a [q]=\"'a' | uppercase\">{{ 'HALLÖCHEN' | uppercase }}</a>")).isEqualTo("<a q=\"A\">HALLÖCHEN</a>");
	}

	@Test
	public void testNull() {
		assertThat(render("{{ null | uppercase }}")).isEqualTo("");
	}
}
