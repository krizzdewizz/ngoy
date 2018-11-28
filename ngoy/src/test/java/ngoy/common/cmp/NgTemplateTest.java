package ngoy.common.cmp;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

import ngoy.ANgoyTest;

public class NgTemplateTest extends ANgoyTest {

	@Test
	public void testIf() {
		assertThat(render("<ng-template [ngIf]=\"true\"><span>bb</span></ng-template>")).isEqualTo("<span>bb</span>");
	}

	@Test
	public void testIfMicro() {
		assertThat(render("<span *ngIf=\"true\">bb</span>")).isEqualTo("<span>bb</span>");
	}
}
