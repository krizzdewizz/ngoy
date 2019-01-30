package ngoy.common;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;
import java.util.Set;

import org.junit.Test;

import ngoy.ANgoyTest;
import ngoy.core.Component;

public class MapPipeTest extends ANgoyTest {

	@Component(selector = "", template = "<ng-container *ngFor=\"let it of entries($map('a', 1, 'b', 2))\">{{it.getKey()}}={{it.getValue()}}</ng-container>")
	public static class MapCmp {
		public Set<Map.Entry<Object, Object>> entries(Map<Object, Object> map) {
			return map.entrySet();
		}
	}

	@Test
	public void test() {
		assertThat(render(MapCmp.class)).isEqualTo("a=1b=2");
	}

	//

	@Component(selector = "", template = "<ng-container *ngFor=\"let it of entries($map('a', null, 'b', 2))\">{{it.getKey()}}={{it.getValue()}}</ng-container>")
	public static class MapNullCmp extends MapCmp {
	}

	@Test
	public void testNull() {
		assertThat(render(MapNullCmp.class)).isEqualTo("a=b=2");
	}
}
