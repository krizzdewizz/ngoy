package ngoy.common.cmp;

import ngoy.ANgoyTest;
import ngoy.core.Component;
import ngoy.core.NgoyException;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.containsString;

public class CompileErrorTest extends ANgoyTest {

	@Component(selector = "", templateUrl = "compile-error.component.html")
	public static class CompileErrorCmp {
	}

	@Test
	public void testText() {
		expectedEx.expect(NgoyException.class);
		expectedEx.expectMessage(containsString("Compile error in"));
		expectedEx.expectMessage(containsString("notAProperty.toLowerCase()"));
		expectedEx.expectMessage(containsString("is neither a method"));
		expectedEx.expectMessage(containsString("compile-error.component.html"));
		expectedEx.expectMessage(containsString("position: [7:3"));
		render(CompileErrorCmp.class);
	}

	//

	@Component(selector = "", templateUrl = "compile-error-style-binding.component.html")
	public static class CompileErrorStyleBindingCmp {
	}

	@Test
	public void testStyle() {
		expectedEx.expect(NgoyException.class);
		expectedEx.expectMessage(containsString("Compile error in"));
		expectedEx.expectMessage(containsString("notAProperty.toLowerCase()"));
		expectedEx.expectMessage(containsString("is neither a method"));
		expectedEx.expectMessage(containsString("compile-error-style-binding.component.html"));
		expectedEx.expectMessage(containsString("position: [7:3"));
		render(CompileErrorStyleBindingCmp.class);
	}

	//

	@Component(selector = "", templateUrl = "compile-error-class-binding.component.html")
	public static class CompileErrorClassBindingCmp {
	}

	@Test
	public void testClass() {
		expectedEx.expect(NgoyException.class);
		expectedEx.expectMessage(containsString("Compile error in"));
		expectedEx.expectMessage(containsString("notAProperty.toLowerCase()"));
		expectedEx.expectMessage(containsString("is neither a method"));
		expectedEx.expectMessage(containsString("compile-error-class-binding.component.html"));
		expectedEx.expectMessage(containsString("position: [7:3"));
		render(CompileErrorClassBindingCmp.class);
	}

	//

	@Component(selector = "", templateUrl = "compile-error-attr-binding.component.html")
	public static class CompileErrorAttrBindingCmp {
	}

	@Test
	public void testAttr() {
		expectedEx.expect(NgoyException.class);
		expectedEx.expectMessage(containsString("Compile error in"));
		expectedEx.expectMessage(containsString("notAProperty.toLowerCase()"));
		expectedEx.expectMessage(containsString("is neither a method"));
		expectedEx.expectMessage(containsString("compile-error-attr-binding.component.html"));
		expectedEx.expectMessage(containsString("position: [7:3"));
		render(CompileErrorAttrBindingCmp.class);
	}
}
