package ngoy.common;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

import ngoy.ANgoyTest;
import ngoy.core.Component;
import ngoy.core.Inject;
import ngoy.core.OnInit;

public class CapitalizePipeTest extends ANgoyTest {
	@Test
	public void test() {
		assertThat(render("{{ 'hello' | capitalize }}")).isEqualTo("Hello");
	}

	@Test
	public void testEmpty() {
		assertThat(render("{{ '' | capitalize }}")).isEqualTo("");
	}

	@Test
	public void testNull() {
		assertThat(render("{{ null | capitalize }}")).isEqualTo("");
	}

	@Component(selector = "test", template = "{{ 'hello' | capitalize }}")
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
