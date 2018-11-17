package ngoy.common.cmp;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.Test;

import ngoy.ANgoyTest;
import ngoy.core.Component;
import ngoy.core.Input;
import ngoy.core.NgModule;

public class IfElseTest extends ANgoyTest {

	@Component(selector = "int", template = "<ng-template #second>second</ng-template><span *ngIf=\"i==0; elseIf i==1 second ; else third  \">first</span><ng-template #third>third</ng-template>")
	public static class IntCmp {
		@Input()
		public int i;
	}

	@Component(selector = "test", template = "<int *ngFor=\"let it of ints\" [i]=\"it\"></int>")
	@NgModule(declarations = { IntCmp.class })
	public static class CmpIf {
		public List<Integer> ints = asList(0, 1, 2);
	}

	@Test
	public void testIf() {
		assertThat(render(CmpIf.class)).isEqualTo("<int><span>first</span></int><int>second</int><int>third</int>");
	}
}
