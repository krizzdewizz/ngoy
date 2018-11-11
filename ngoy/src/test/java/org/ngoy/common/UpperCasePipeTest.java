package org.ngoy.common;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;
import org.ngoy.ANgoyTest;
import org.ngoy.core.Component;
import org.ngoy.core.NgModule;

public class UpperCasePipeTest extends ANgoyTest {

	@Component(selector = "test", template = "{{ 'HALLÖCHEN' | uppercase }}")
	@NgModule(declarations = { UpperCasePipe.class })
	public static class Cmp {
	}

	@Test
	public void test() {
		assertThat(render(Cmp.class)).isEqualTo("HALL&Ouml;CHEN");
	}
}
