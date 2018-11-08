package org.ngoy.common;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;
import org.ngoy.common.UpperCasePipe;
import org.ngoy.core.Component;

public class UpperCasePipeTest extends ANgoyTest {

	@Component(selector = "test", declarations = { UpperCasePipe.class }, template = "{{ 'HALLÖCHEN' | uppercase }}")
	public static class Cmp {
	}

	@Test
	public void test() {
		assertThat(render(Cmp.class)).isEqualTo("HALL&Ouml;CHEN");
	}
}
