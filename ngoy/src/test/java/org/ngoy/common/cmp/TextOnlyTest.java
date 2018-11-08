package org.ngoy.common.cmp;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;
import org.ngoy.common.ANgoyTest;
import org.ngoy.core.Component;

public class TextOnlyTest extends ANgoyTest {

	@Component(selector = "test", template = "hello")
	public static class Cmp {
	}

	@Test
	public void test() {
		assertThat(render(Cmp.class)).isEqualTo("hello");
	}
}
