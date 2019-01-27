package ngoy.common;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

import ngoy.ANgoyTest;
import ngoy.core.Component;

public class RawPipeTest extends ANgoyTest {
	@Component(selector = "a-cmp", template = "a{{ rawText | raw  }}a")
	public static class RawCmp {
		public String rawText = "<>";
	}

	@Test
	public void testRaw() {
		assertThat(render(RawCmp.class)).isEqualTo("a<>a");
	}

	//

	@Component(selector = "a-cmp", template = "a{{ $raw(rawText) }}a")
	public static class FunCmp {
		public String rawText = "x";
	}

	@Test
	public void testFun() {
		assertThat(render(FunCmp.class)).isEqualTo("axa");
	}

	//

	@Component(selector = "a-cmp", template = "a{{ rawText | raw  }}a")
	public static class NullCmp {
		public String rawText;
	}

	@Test
	public void testNull() {
		assertThat(render(NullCmp.class)).isEqualTo("aa");
	}

}
