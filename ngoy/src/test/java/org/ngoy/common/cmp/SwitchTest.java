package org.ngoy.common.cmp;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;
import org.ngoy.ANgoyTest;
import org.ngoy.core.Component;
import org.ngoy.core.Input;
import org.ngoy.core.NgModule;

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
}
