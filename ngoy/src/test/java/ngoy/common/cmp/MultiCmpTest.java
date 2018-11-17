package ngoy.common.cmp;

import static org.hamcrest.CoreMatchers.containsString;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import ngoy.ANgoyTest;
import ngoy.core.Component;
import ngoy.core.NgModule;
import ngoy.core.NgoyException;

public class MultiCmpTest extends ANgoyTest {

	@Component(selector = "person", template = "")
	public static class PersonCmp {
	}

	@Component(selector = "person", template = "")
	public static class Person2Cmp {
	}

	@Component(selector = "test", template = "<person></person>")
	@NgModule(declarations = { PersonCmp.class, Person2Cmp.class })
	public static class Cmp {
	}

	@Rule
	public ExpectedException expectedEx = ExpectedException.none();

	@Test
	public void test() {
		expectedEx.expect(NgoyException.class);
		expectedEx.expectMessage(containsString("More than one component matched on the selector 'person'"));
		render(Cmp.class);
	}
}
