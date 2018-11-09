package org.ngoy.core;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;
import org.ngoy.ANgoyTest;

public class DepCycleTest extends ANgoyTest {
	
	public static class ServiceA {
		@Inject
		public ServiceB b;
	}

	public static class ServiceB {
		@Inject
		public ServiceA a;
	}

	@Component(selector = "test", providers = { ServiceA.class, ServiceB.class })
	public static class Cmp {
		@Inject
		public ServiceA a;
	}

	@Test
	public void test() {
		assertThat(render(Cmp.class)).isEqualTo("");
	}
}
