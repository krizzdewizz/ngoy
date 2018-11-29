package ngoy.common.cmp;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

import ngoy.ANgoyTest;
import ngoy.core.Component;
import ngoy.core.Input;
import ngoy.core.NgModule;

public class SwitchTest extends ANgoyTest {

	@Component(selector = "emoy", template = "" //
			+ "<h1 [ngSwitch]=\"emotion\">" + //
			"<ng-template [ngSwitchCase]=\"'happy'\"><div>HAPPY</div></ng-template>" + //
			"<ng-template [ngSwitchCase]=\"'sad'\"><div>SAD</div></ng-template>" + //
			"<ng-template ngSwitchDefault><div>NONE</div></ng-template>" + //
			"</h1>")
	public static class EmoyCmp {
		@Input()
		public String emotion = "sad";
	}

	@Component(selector = "test", template = "<emoy></emoy>")
	@NgModule(declarations = { EmoyCmp.class })
	public static class SwitchCmp {
	}

	@Test
	public void test() {
		assertThat(render(SwitchCmp.class)).isEqualTo("<emoy><h1><div>SAD</div></h1></emoy>");
	}

	//

	@Component(selector = "emoy", template = "" //
			+ "<h1 [ngSwitch]=\"emotion\">" + //
			"<ng-template [ngSwitchCase]=\"'happy'\"><div>HAPPY</div></ng-template>" + //
			"<ng-template [ngSwitchCase]=\"'sad'\"><div>SAD</div></ng-template>" + //
			"</h1>")
	public static class EmoyNoDefaultCmp {
		@Input()
		public String emotion = "x";
	}

	@Component(selector = "test", template = "<emoy></emoy>")
	@NgModule(declarations = { EmoyNoDefaultCmp.class })
	public static class SwitchNoDefaultCmp {
	}

	@Test
	public void testNoDefault() {
		assertThat(render(SwitchNoDefaultCmp.class)).isEqualTo("<emoy><h1></h1></emoy>");
	}
}
