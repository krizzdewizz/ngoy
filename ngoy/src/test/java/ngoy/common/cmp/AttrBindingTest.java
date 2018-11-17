package ngoy.common.cmp;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.CoreMatchers.containsString;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import ngoy.ANgoyTest;
import ngoy.core.Component;
import ngoy.core.Input;
import ngoy.core.NgModule;
import ngoy.core.NgoyException;

public class AttrBindingTest extends ANgoyTest {

	@Rule
	public ExpectedException expectedEx = ExpectedException.none();

	@Component(selector = "person", template = "hello: {{name}}{{title}}")
	public static class PersonCmp {
		@Input()
		public String name;

		@Input()
		public String title;
	}

	@Component(selector = "test", template = "<person name=\"x\" [title]=\"'abc'\"></person>")
	@NgModule(declarations = { PersonCmp.class })
	public static class Attr {
	}

	@Test
	public void testAttr() {
		assertThat(render(Attr.class)).isEqualTo("<person>hello: xabc</person>");
	}

	//

	@Component(selector = "person", template = "hello: {{title}}{{title2}}{{title3}}{{title4}}")
	public static class Person2Cmp {
		@Input()
		public String title;

		@Input()
		public String title2;

		@Input("q")
		public String title3;

		@Input("r")
		public String title4;
	}

	@Component(selector = "test", template = "<person title=\"abc\" [title2]=\"'def'\" q=\"ghi\" [r]=\"'jkl'\"></person>")
	@NgModule(declarations = { Person2Cmp.class })
	public static class Attr2 {
	}

	@Test
	public void testAttr2() {
		assertThat(render(Attr2.class)).isEqualTo("<person>hello: abcdefghijkl</person>");
	}

	//

	@Component(selector = "person", template = "hello: {{title}}{{title2}}{{title3}}{{title4}}")
	public static class PersonNonStringCmp {
		@Input()
		public int number;
	}

	@Component(selector = "test", template = "<person number=\"0\"></person>")
	@NgModule(declarations = { PersonNonStringCmp.class })
	public static class AttrNonString {
	}

	@Test
	public void testAttrNonString() {
		expectedEx.expect(NgoyException.class);
		expectedEx.expectMessage(containsString("but would receive a string"));
		render(AttrNonString.class);
	}

	//

	@Component(selector = "person", template = "hello: {{title}}{{title2}}{{title3}}{{title4}}")
	public static class PersonNonStringParamCmp {
		@Input()
		public void setNumber(@SuppressWarnings("unused") int n) {
		}
	}

	@Component(selector = "test", template = "<person number=\"0\"></person>")
	@NgModule(declarations = { PersonNonStringParamCmp.class })
	public static class AttrNonStringParam {
	}

	@Test
	public void testAttrNonStringParam() {
		expectedEx.expect(NgoyException.class);
		expectedEx.expectMessage(containsString("but would receive a string"));
		render(AttrNonStringParam.class);
	}
}