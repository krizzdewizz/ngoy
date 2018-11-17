package org.ngoy.core.internal;

import static org.hamcrest.CoreMatchers.containsString;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.ngoy.ANgoyTest;
import org.ngoy.core.Component;
import org.ngoy.core.NgModule;
import org.ngoy.core.NgoyException;

public class InvalidSyntaxTest extends ANgoyTest {

	@Component(selector = "nested", templateUrl = "invalid-syntax-nested.html")
	public static class CmpNested {
	}

	@Component(selector = "test", templateUrl = "invalid-syntax-nested-app.html")
	@NgModule(declarations = { CmpNested.class })
	public static class CmpBinding {
	}

	@Rule
	public ExpectedException expectedEx = ExpectedException.none();

	@Test
	public void testBinding() {
		expectedEx.expect(NgoyException.class);
		expectedEx.expectMessage(containsString(CmpNested.class.getName()));
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
		expectedEx.expectMessage(containsString(CmpNestedObj.class.getName()));
		expectedEx.expectMessage(containsString("templateUrl: invalid-syntax-obj.html"));
		expectedEx.expectMessage(containsString("position: [8:")); // line
		render(CmpObjBinding.class);
	}
}
