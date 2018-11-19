package ngoy.common;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

import ngoy.ANgoyTest;
import ngoy.core.Component;
import ngoy.core.NgModule;
import ngoy.core.Pipe;
import ngoy.core.PipeTransform;

public class PipeParamsTest extends ANgoyTest {

	@Pipe("myPipe")
	public static class MyPipe implements PipeTransform {
		@Override
		public Object transform(Object obj, Object... params) {
			return String.valueOf(obj) + params[0] + params[1];
		}
	}

	@Component(selector = "test", template = "{{ 'alfred' | myPipe:'a':age }}")
	@NgModule(declarations = { MyPipe.class })
	public static class Cmp {
		public int age = 22;
	}

	@Test
	public void test() {
		assertThat(render(Cmp.class)).isEqualTo("alfreda22");
	}
}
