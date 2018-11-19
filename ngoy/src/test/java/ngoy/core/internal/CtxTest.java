package ngoy.core.internal;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

public class CtxTest {
	@Test
	public void testNewLine() {
		Ctx ctx = Ctx.of();
		String eval = (String) ctx.eval("'\n'");

		assertThat(eval).isEqualTo("\n");
	}
}
