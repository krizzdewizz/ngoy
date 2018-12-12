package ngoy.core;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

public class ContextTest {

	@Test
	public void testOf() {
		assertThat(Context.of()).isNotNull();
	}

	@Test
	public void testOfRoot() {
		assertThat(Context.of(Object.class, new Object())).isNotNull();
	}

	@Test
	public void testVariable() {
		Context<?> of = Context.of();
		Context<?> context = of.variable("a", String.class, "1");
		assertThat(context).isSameAs(of);
	}
}
