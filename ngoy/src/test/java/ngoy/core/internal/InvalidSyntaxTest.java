package ngoy.core.internal;

import ngoy.ANgoyTest;
import ngoy.core.Component;
import ngoy.core.NgModule;
import ngoy.core.NgoyException;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.containsString;

public class InvalidSyntaxTest extends ANgoyTest {

	@Component(selector = "nested", templateUrl = "invalid-syntax-nested.html")
	public static class CmpNested {
	}

	@Component(selector = "test", templateUrl = "invalid-syntax-nested-app.html")
	@NgModule(declarations = { CmpNested.class })
	public static class CmpBinding {
	}

	@Test
	public void testBinding() {
		expectedEx.expect(NgoyException.class);
		expectedEx.expectMessage(containsString("templateUrl: invalid-syntax-nested.html"));
		expectedEx.expectMessage(containsString("position: [11:")); // line
		render(CmpBinding.class);
	}

	//

	@Component(selector = "nested", templateUrl = "invalid-syntax-obj.html")
	public static class CmpNestedObj {
	}

	@Component(selector = "test", template = "<nested></nested>")
	@NgModule(declarations = { CmpNestedObj.class })
	public static class CmpObjBinding {
	}

	@Test
	public void testObjBinding() {
		expectedEx.expect(NgoyException.class);
		expectedEx.expectMessage(containsString("Unexpected token"));
		expectedEx.expectMessage(containsString("templateUrl: invalid-syntax-obj.html"));
		expectedEx.expectMessage(containsString("position: [8:")); //
		render(CmpObjBinding.class);
	}
}
