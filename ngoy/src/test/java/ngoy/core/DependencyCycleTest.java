package ngoy.core;

import ngoy.ANgoyTest;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.containsString;

public class DependencyCycleTest extends ANgoyTest {

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
		expectedEx.expect(NgoyException.class);
		expectedEx.expectMessage(containsString("Dependency cycle detected"));
		render(Cmp.class);
	}

	@Component(selector = "test", providers = { ServiceA.class, ServiceB.class })
	public static class CmpCtor {
		public CmpCtor(ServiceA a) {
			String.valueOf(a);
		}
	}

	@Test
	public void testCtor() {
		expectedEx.expect(NgoyException.class);
		expectedEx.expectMessage(containsString("Dependency cycle detected"));
		render(CmpCtor.class);
	}
}
