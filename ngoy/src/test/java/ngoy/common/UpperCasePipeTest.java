package ngoy.common;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

import ngoy.ANgoyTest;
import ngoy.core.Component;

public class UpperCasePipeTest extends ANgoyTest {

	@Component(selector = "", template = "<a [q]='\"a\" | uppercase'>{{ \"HALLÖCHEN\" | uppercase }}</a>")
	public static class UpperCmp {
	}

	@Test
	public void test() {
		assertThat(render(UpperCmp.class)).isEqualTo("<a q=\"A\">HALLÖCHEN</a>");
	}

	//

	@Component(selector = "", template = "{{ null | uppercase }}")
	public static class NullCmp {
	}

	@Test
	public void testNull() {
		assertThat(render(NullCmp.class)).isEqualTo("");
	}
}
