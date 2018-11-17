package ngoy.common.cmp;

import static ngoy.core.Provider.useValue;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.junit.Test;
import org.mockito.Mock;

import ngoy.ANgoyTest;
import ngoy.core.Component;
import ngoy.core.Inject;
import ngoy.core.NgModule;
import ngoy.core.OnDestroy;
import ngoy.core.OnInit;

public class LifecycleTest extends ANgoyTest {

	interface Service {
		void init();

		void destroy();
	}

	public static abstract class ACmp implements OnInit, OnDestroy {
		@Inject
		public Service service;

		@Override
		public void ngOnInit() {
			service.init();
		}

		@Override
		public void ngOnDestroy() {
			service.destroy();
		}
	}

	@Component(selector = "person", template = "hello")
	public static class PersonCmp extends ACmp {
	}

	@Component(selector = "test", template = "<person></person><person></person>")
	@NgModule(declarations = { PersonCmp.class })
	public static class Lifecycle extends ACmp {
	}

	@Mock
	private Service service;

	@Test
	public void testLifecycle() {
		render(Lifecycle.class, useValue(Service.class, service));
		verify(service, times(3)).init();
		verify(service, times(3)).destroy();
	}
}
