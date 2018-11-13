package org.ngoy.common;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;
import org.ngoy.ANgoyTest;
import org.ngoy.core.Component;
import org.ngoy.core.NgModule;
import org.ngoy.core.Pipe;
import org.ngoy.core.PipeTransform;

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

	//

	@Component(selector = "test", template = "{{ 'alfred' | myPipe:'a:x':age }}")
	@NgModule(declarations = { MyPipe.class })
	public static class FailCmp {
		public int age = 22;
	}

	@Test
	public void testFail() {
		assertThat(render(FailCmp.class)).isEqualTo("alfreda:x22");
	}
}
