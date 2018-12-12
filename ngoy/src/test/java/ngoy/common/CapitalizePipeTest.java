package ngoy.common;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

import ngoy.ANgoyTest;
import ngoy.core.Component;
import ngoy.core.Inject;
import ngoy.core.OnInit;

public class CapitalizePipeTest extends ANgoyTest {

	@Component(selector = "", template = "{{ \"hello\" | capitalize }}")
	public static class HelloCmp {
	}

	@Test
	public void test() {
		assertThat(render(HelloCmp.class)).isEqualTo("Hello");
	}

	//

	@Component(selector = "", template = "{{ \"\" | capitalize }}")
	public static class EmptyCmp {
	}

	@Test
	public void testEmpty() {
		assertThat(render(EmptyCmp.class)).isEqualTo("");
	}

	//

	@Component(selector = "", template = "{{ null | capitalize }}")
	public static class NullCmp {
	}

	@Test
	public void testNull() {
		assertThat(render(NullCmp.class)).isEqualTo("");
	}

	@Component(selector = "test", template = "{{ \"hello\" | capitalize }}")
	public static class NoLocaleCmp implements OnInit {
		@Inject
		public CapitalizePipe pipe;

		@Override
		public void ngOnInit() {
			pipe.localeProvider = null;
		}
	}

	@Test
	public void testNoLocale() {
		assertThat(render(NoLocaleCmp.class)).isEqualTo("Hello");
	}
}
