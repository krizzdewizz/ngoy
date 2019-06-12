package ngoy.common;

import ngoy.ANgoyTest;
import ngoy.core.Component;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class SetPipeTest extends ANgoyTest {

	@Component(selector = "", template = "<ng-container *ngFor=\"let it of $set('a', 'b', 'a')\">{{it}}</ng-container>")
	public static class SetCmp {
	}

	@Test
	public void test() {
		assertThat(render(SetCmp.class)).isEqualTo("ab");
	}

	//

	@Component(selector = "", template = "<ng-container *ngFor=\"let it of $set(null, 'a', null )\">{{it}}</ng-container>")
	public static class SetNullCmp {
	}

	@Test
	public void testNull() {
		assertThat(render(SetNullCmp.class)).isEqualTo("a");
	}
}
