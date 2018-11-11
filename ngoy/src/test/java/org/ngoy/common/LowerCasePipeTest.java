package org.ngoy.common;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;
import org.ngoy.ANgoyTest;
import org.ngoy.core.Component;
import org.ngoy.core.NgModule;

public class LowerCasePipeTest extends ANgoyTest {

	@Component(selector = "test", template = "{{ 'HALLÖCHEN' | lowercase }}")
	@NgModule(declarations = { LowerCasePipe.class })
	public static class Cmp {
	}

	@Test
	public void test() {
		assertThat(render(Cmp.class)).isEqualTo("hall&ouml;chen");
	}
}
