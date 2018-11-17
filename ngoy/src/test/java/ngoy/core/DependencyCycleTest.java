package ngoy.core;

import static org.hamcrest.CoreMatchers.containsString;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import ngoy.ANgoyTest;
import ngoy.core.Component;
import ngoy.core.Inject;
import ngoy.core.NgoyException;

public class DependencyCycleTest extends ANgoyTest {

	public static class ServiceA {
		@Inject
		public ServiceB b;
	}

	public static class ServiceB {
		@Inject
		public ServiceA a;
	}

	@Rule
	public ExpectedException expectedEx = ExpectedException.none();

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