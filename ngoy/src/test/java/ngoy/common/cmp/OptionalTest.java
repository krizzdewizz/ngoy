package ngoy.common.cmp;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.CoreMatchers.containsString;

import org.junit.Test;

import ngoy.ANgoyTest;
import ngoy.core.Component;
import ngoy.core.Inject;
import ngoy.core.NgoyException;
import ngoy.core.Optional;
import ngoy.service.TestService;

public class OptionalTest extends ANgoyTest {

	@Component(selector = "test", template = "{{ service == null }}")
	public static class Cmp {

		public TestService<String> service;

		@Inject
		@Optional
		public void setSetIt(TestService<String> service) {
			this.service = service;
		}
	}

	@Test
	public void test() {
		assertThat(render(Cmp.class)).isEqualTo("true");
	}

	//

	public static class QService {
	}

	@Component(selector = "test", template = "{{ service == null }}{{ qService != null }}", providers = { QService.class })
	public static class CtorCmp {
		public TestService<String> service;
		public QService qService;

		public CtorCmp(QService qService, @Optional TestService<String> service) {
			this.service = service;
			this.qService = qService;
		}
	}

	@Test
	public void ctor() {
		assertThat(render(CtorCmp.class)).isEqualTo("truetrue");
	}

	//

	@Component(selector = "test", template = "")
	public static class NoProviderFoundCmp {
		@Inject
		public void setSetIt(@SuppressWarnings("unused") TestService<String> service) {
		}
	}

	@Test
	public void noProviderFound() {
		expectedEx.expect(NgoyException.class);
		expectedEx.expectMessage(containsString("No provider found"));
		expectedEx.expectMessage(containsString(TestService.class.getName()));
		render(NoProviderFoundCmp.class);
	}

	//

	@Component(selector = "test", template = "")
	public static class CtorMissingCmp {
		public CtorMissingCmp(@SuppressWarnings("unused") TestService<String> service) {
		}
	}

	@Test
	public void ctorMissingCmp() {
		expectedEx.expect(NgoyException.class);
		expectedEx.expectMessage(containsString("No provider found"));
		expectedEx.expectMessage(containsString(TestService.class.getName()));
		render(CtorMissingCmp.class);
	}
}
