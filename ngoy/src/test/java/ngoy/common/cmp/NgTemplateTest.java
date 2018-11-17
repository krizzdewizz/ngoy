package ngoy.common.cmp;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

import ngoy.ANgoyTest;
import ngoy.core.Component;

public class NgTemplateTest extends ANgoyTest {

	@Component(selector = "test", template = "<ng-template [ngIf]=\"true\"><span>bb</span></ng-template>")
	public static class CmpIf {
	}

	@Test
	public void testIf() {
		assertThat(render(CmpIf.class)).isEqualTo("<span>bb</span>");
	}

	//

	@Component(selector = "test", template = "<span *ngIf=\"true\">bb</span>")
	public static class CmpIfMicro {
	}

	@Test
	public void testIfMicro() {
		assertThat(render(CmpIf.class)).isEqualTo("<span>bb</span>");
	}
}
