package ngoy.common;

import ngoy.ANgoyTest;
import ngoy.core.Component;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class ListPipeTest extends ANgoyTest {

	@Component(selector = "", template = "<ng-container *ngFor=\"let it of $list('a', 'b')\">{{it}}</ng-container>")
	public static class ListCmp {
	}

	@Test
	public void test() {
		assertThat(render(ListCmp.class)).isEqualTo("ab");
	}

	//

	@Component(selector = "", template = "<ng-container *ngFor=\"let it of $list(null, 'a', null )\">{{it}}</ng-container>")
	public static class ListNullCmp {
	}

	@Test
	public void testNull() {
		assertThat(render(ListNullCmp.class)).isEqualTo("a");
	}
}
