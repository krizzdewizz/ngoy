package org.ngoy.common.cmp;

import static org.assertj.core.api.Assertions.assertThat;
import static org.ngoy.core.Provider.useValue;

import org.junit.Test;
import org.ngoy.common.ANgoyTest;
import org.ngoy.core.Component;
import org.ngoy.core.HostBinding;
import org.ngoy.core.Inject;
import org.ngoy.core.OnInit;
import org.ngoy.service.TestService;

public class HostBindingAttrTest extends ANgoyTest {

	@Component(selector = "person", template = "")
	public static class PersonCmp implements OnInit {
		@Inject
		public TestService<String> service;

		@HostBinding("attr.x")
		public Object xValue;

		@Override
		public void ngOnInit() {
			xValue = service.value;
		}
	}

	@Component(selector = "test", declarations = { PersonCmp.class }, template = "<person></person>")
	public static class Cmp {
	}

	@Test
	public void testHello() {
		assertThat(render(Cmp.class, useValue(TestService.class, TestService.of("hello")))).isEqualTo("<person x=\"hello\"></person>");
	}

	@Test
	public void testNull() {
		assertThat(render(Cmp.class, useValue(TestService.class, TestService.of(null)))).isEqualTo("<person></person>");
	}
}
