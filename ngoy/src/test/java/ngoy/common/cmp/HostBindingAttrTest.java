package ngoy.common.cmp;

import static ngoy.core.Provider.useValue;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

import ngoy.ANgoyTest;
import ngoy.core.Component;
import ngoy.core.HostBinding;
import ngoy.core.Inject;
import ngoy.core.NgModule;
import ngoy.core.OnInit;
import ngoy.service.TestService;

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

	@Component(selector = "test", template = "<person></person>")
	@NgModule(declarations = { PersonCmp.class })
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
