package org.ngoy.common.cmp;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.instanceOf;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.ngoy.ANgoyTest;
import org.ngoy.core.Component;
import org.ngoy.core.Input;
import org.ngoy.core.NgModule;
import org.ngoy.core.NgoyException;

public class RuntimeErrorsTest extends ANgoyTest {

	@Rule
	public ExpectedException expectedEx = ExpectedException.none();

	@Component(selector = "person", template = "")
	public static class PersonIntCmp {
		@Input()
		public int age;
	}

	@Component(selector = "test", template = "<person [age]=\"a\"></person>")
	@NgModule(declarations = { PersonIntCmp.class })
	public static class Cmp {
	}

	@Test
	public void test() {
		expectedEx.expect(NgoyException.class);
		expectedEx.expectMessage(containsString("Neither getter method nor field found for property 'a'"));
		render(Cmp.class);
	}

	//

	@Component(selector = "person", template = "")
	public static class PersonSetterCmp {
		@Input()
		public int age(int age) throws Exception {
			throw new Exception("xxx");
		};
	}

	@Component(selector = "test", template = "<person [age]=\"2\"></person>")
	@NgModule(declarations = { PersonSetterCmp.class })
	public static class CmpSetter {
	}

	@Test
	public void testSetter() {
		expectedEx.expect(NgoyException.class);
		expectedEx.expectMessage(containsString("Error while invoking input setter"));
		expectedEx.expectCause(instanceOf(Exception.class));
		render(CmpSetter.class);
	}

	//

	@Component(selector = "person", template = "")
	public static class PersonInputWrongTypeCmp {
		@Input()
		public int age;
	}

	@Component(selector = "test", template = "<person [age]=\"ageString\"></person>")
	@NgModule(declarations = { PersonInputWrongTypeCmp.class })
	public static class CmpInputWrongType {
		public String ageString = "49";
	}

	@Test
	public void testInputWrongType() {
		expectedEx.expect(NgoyException.class);
		expectedEx.expectMessage(containsString("Error while setting input field"));
		render(CmpInputWrongType.class);
	}
}
