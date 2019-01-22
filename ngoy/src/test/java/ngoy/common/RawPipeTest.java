package ngoy.common;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

import ngoy.ANgoyTest;
import ngoy.core.Component;
import ngoy.core.Inject;
import ngoy.core.OnDestroy;
import ngoy.core.OnInit;

public class RawPipeTest extends ANgoyTest {
	@Component(selector = "a-cmp", template = "a{{ rawText | raw  }}a")
	public static class RawCmp {
		public String rawText = "<>";
	}

	@Test
	public void testRaw() {
		assertThat(render(RawCmp.class)).isEqualTo("a<>a");
	}

	@Component(selector = "a-cmp", template = "a{{ rawText | raw  }}a")
	public static class NullCmp {
		public String rawText;
	}

	@Test
	public void testNull() {
		assertThat(render(NullCmp.class)).isEqualTo("aa");
	}

	//

	@Component(selector = "", template = "a")
	public static class CtxSetIfUnusedCmp implements OnInit, OnDestroy {
		@Inject
		public RawPipe rawPipe;

		@Override
		public void ngOnInit() {
			rawPipe.transform("<b>");
		}

		@Override
		public void ngOnDestroy() {
			rawPipe.transform("</b>");
		}
	}

	@Test
	public void testCtxSetIfUnused() {
		assertThat(render(CtxSetIfUnusedCmp.class)).isEqualTo("<b>a</b>");
	}
}
