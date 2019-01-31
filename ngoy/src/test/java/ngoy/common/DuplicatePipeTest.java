package ngoy.common;

import static org.hamcrest.CoreMatchers.containsString;

import org.junit.Test;

import ngoy.ANgoyTest;
import ngoy.core.Component;
import ngoy.core.NgModule;
import ngoy.core.NgoyException;
import ngoy.core.Pipe;
import ngoy.core.PipeTransform;

public class DuplicatePipeTest extends ANgoyTest {

	@Pipe("set")
	public static class MySetPipe implements PipeTransform {
		@Override
		public Object transform(Object obj, Object... params) {
			return null;
		}
	}

	@Component(selector = "test", template = "")
	@NgModule(declarations = { MySetPipe.class })
	public static class Cmp {
	}

	@Test
	public void test() {
		expectedEx.expect(NgoyException.class);
		expectedEx.expectMessage(containsString("More than one provider found for pipe 'set': ngoy.common.CommonModule, ngoy.common.DuplicatePipeTest$Cmp"));

		render(Cmp.class);
	}
}
