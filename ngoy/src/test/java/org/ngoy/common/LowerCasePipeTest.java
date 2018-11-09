package org.ngoy.common;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;
import org.ngoy.ANgoyTest;
import org.ngoy.common.LowerCasePipe;
import org.ngoy.core.Component;

public class LowerCasePipeTest extends ANgoyTest {

	@Component(selector = "test", declarations = { LowerCasePipe.class }, template = "{{ 'HALLÖCHEN' | lowercase }}")
	public static class Cmp {
	}

	@Test
	public void test() {
		assertThat(render(Cmp.class)).isEqualTo("hall&ouml;chen");
	}
}
