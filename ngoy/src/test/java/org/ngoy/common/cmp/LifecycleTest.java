package org.ngoy.common.cmp;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.ngoy.core.Provider.useValue;

import org.junit.Test;
import org.mockito.Mock;
import org.ngoy.ANgoyTest;
import org.ngoy.core.Component;
import org.ngoy.core.Inject;
import org.ngoy.core.OnDestroy;
import org.ngoy.core.OnInit;

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

	@Component(selector = "test", template = "<person></person><person></person>", declarations = { PersonCmp.class })
	public static class Attr extends ACmp {
	}

	@Mock
	private Service service;

	@Test
	public void testAttr() {
		render(Attr.class, useValue(Service.class, service));
		verify(service, times(3)).init();
		verify(service, times(3)).destroy();
	}
}
