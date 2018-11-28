package ngoy.common;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.CoreMatchers.containsString;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import ngoy.ANgoyTest;
import ngoy.core.Component;
import ngoy.core.NgModule;
import ngoy.core.NgoyException;
import ngoy.core.Pipe;
import ngoy.core.PipeTransform;

public class PipeParamsTest extends ANgoyTest {

	@Rule
	public ExpectedException expectedEx = ExpectedException.none();

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

	@Component(selector = "test", template = "{{ ((true || false) ? 'alfred' : 'qbert') | uppercase }}")
	@NgModule(declarations = { MyPipe.class })
	public static class PipeWithOrCmp {
	}

	@Test
	public void testPipeWithOr() {
		assertThat(render(PipeWithOrCmp.class)).isEqualTo("ALFRED");
	}

	//

	@Component(selector = "test", template = "{{ $myPipe('alfred', 'a', age) }}")
	@NgModule(declarations = { MyPipe.class })
	public static class FuncCallCmp {
		public int age = 22;
	}

	@Test
	public void testFuncCall() {
		assertThat(render(FuncCallCmp.class)).isEqualTo("alfreda22");
	}

	//

	@Component(selector = "test", template = "{{ $myPipe() }}")
	@NgModule(declarations = { MyPipe.class })
	public static class FuncCallMissingArgCmp {
	}

	@Test
	public void testFuncCallMissingArg() {
		expectedEx.expect(NgoyException.class);
		expectedEx.expectMessage(containsString("Missing first argument"));
		expectedEx.expectMessage(containsString("$myPipe"));
		render(FuncCallMissingArgCmp.class);
	}

	//

	@Component(selector = "test", template = "{{ $unknownPipe('a') }}")
	@NgModule(declarations = { MyPipe.class })
	public static class UnknownPipeCmp {
	}

	@Test
	public void testUnknownPipe() {
		expectedEx.expect(NgoyException.class);
		expectedEx.expectMessage(containsString("No provider for pipe"));
		expectedEx.expectMessage(containsString("unknownPipe"));
		render(UnknownPipeCmp.class);
	}
}
