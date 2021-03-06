package ngoy.common.cmp;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

import ngoy.ANgoyTest;
import ngoy.core.Component;

public class NgTemplateTest extends ANgoyTest {

	@Component(selector = "", template = "<ng-template [ngIf]=\"true\"><span>bb</span></ng-template>")
	public static class IfCmp {
	}

	@Test
	public void testIf() {
		assertThat(render(IfCmp.class)).isEqualTo("<span>bb</span>");
	}

	//

	@Component(selector = "", template = "<span *ngIf=\"true\">bb</span>")
	public static class MicroIfCmp {
	}

	@Test
	public void testIfMicro() {
		assertThat(render(MicroIfCmp.class)).isEqualTo("<span>bb</span>");
	}
}
