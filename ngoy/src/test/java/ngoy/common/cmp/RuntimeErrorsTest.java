package ngoy.common.cmp;

import ngoy.ANgoyTest;
import ngoy.core.Component;
import ngoy.core.Inject;
import ngoy.core.Input;
import ngoy.core.NgModule;
import ngoy.core.NgoyException;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.instanceOf;

public class RuntimeErrorsTest extends ANgoyTest {

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
		expectedEx.expectMessage(containsString("\"a\" is neither a method, a field"));
		render(Cmp.class);
	}

	//

	@Component(selector = "person", template = "")
	public static class PersonSetterCmp {
		@Input()
		public int age(int age) throws Exception {
			throw new Exception("xxx" + age);
		};
	}

	@Component(selector = "test", template = "<person [age]=\"2\"></person>")
	@NgModule(declarations = { PersonSetterCmp.class })
	public static class CmpSetter {
	}

	@Test
	public void testSetter() {
		expectedEx.expect(NgoyException.class);
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
		expectedEx.expectMessage(containsString("Assignment conversion not possible"));
		render(CmpInputWrongType.class);
	}

	//

	@Component(selector = "test", template = "<a *ngIf='\"\"'/>")
	public static class CmpNotBoolean {
	}

	@Test
	public void testNotBoolean() {
		expectedEx.expect(NgoyException.class);
		expectedEx.expectMessage(containsString("Not a boolean expression"));
		render(CmpNotBoolean.class);
	}

	//

	@Component(selector = "a", template = "")
	public static class CmpFieldInjectFinal {
		@Inject
		public final String x = null;
	}

	@Component(selector = "test", template = "<a></a>")
	@NgModule(declarations = { CmpFieldInjectFinal.class })
	public static class CmpFieldInjectFinalTest {
	}

	@Test
	public void testFieldInjectFinal() {
		expectedEx.expect(NgoyException.class);
		expectedEx.expectMessage(containsString("@Inject annotated field must be public, non-final, non-static"));
		render(CmpFieldInjectFinalTest.class);
	}

	//

	@Component(selector = "a", template = "")
	public static class CmpFieldInjectStatic {
		@Inject
		public static String x;
	}

	@Component(selector = "test", template = "<a></a>")
	@NgModule(declarations = { CmpFieldInjectStatic.class })
	public static class CmpFieldInjectStaticTest {
	}

	@Test
	public void testFieldInjectStatic() {
		expectedEx.expect(NgoyException.class);
		expectedEx.expectMessage(containsString("@Inject annotated field must be public, non-final, non-static"));
		render(CmpFieldInjectStaticTest.class);
	}

	//

	@Component(selector = "a", template = "")
	public static class CmpMethodInjectStatic {
		@Inject
		public static void setX(@SuppressWarnings("unused") String x) {
		}
	}

	@Component(selector = "test", template = "<a></a>")
	@NgModule(declarations = { CmpMethodInjectStatic.class })
	public static class CmpMethodInjectStaticTest {
	}

	@Test
	public void testMethodInjectStatic() {
		expectedEx.expect(NgoyException.class);
		expectedEx.expectMessage(containsString("@Inject annotated method must be public, non-static"));
		render(CmpMethodInjectStaticTest.class);
	}

	//

	@Component(selector = "a", template = "")
	public static class CmpMethodInputStatic {
		@Input
		public static void setX(@SuppressWarnings("unused") String x) {
		}
	}

	@Component(selector = "test", template = "<a x=\"'b'\"></a>")
	@NgModule(declarations = { CmpMethodInputStatic.class })
	public static class CmpMethodInputStaticTest {
	}

	@Test
	public void testMethodInputStatic() {
		expectedEx.expect(NgoyException.class);
		expectedEx.expectMessage(containsString("@Input annotated method must be public, non-static"));
		render(CmpMethodInputStaticTest.class);
	}

	//

	@Component(selector = "a", template = "")
	public static class CmpFieldInputStatic {
		@Input
		public static String x;
	}

	@Component(selector = "test", template = "<a x=\"'b'\"></a>")
	@NgModule(declarations = { CmpFieldInputStatic.class })
	public static class CmpFieldInputStaticTest {
	}

	@Test
	public void testFieldInputStatic() {
		expectedEx.expect(NgoyException.class);
		expectedEx.expectMessage(containsString("@Input annotated field must be public, non-final, non-static"));
		render(CmpFieldInputStaticTest.class);
	}

	//

	@Component(selector = "a", template = "")
	public static class CmpFieldInputFinal {
		@Input
		public final String x = null;
	}

	@Component(selector = "test", template = "<a x=\"'b'\"></a>")
	@NgModule(declarations = { CmpFieldInputFinal.class })
	public static class CmpFieldInputFinalTest {
	}

	@Test
	public void testFieldInputFinal() {
		expectedEx.expect(NgoyException.class);
		expectedEx.expectMessage(containsString("@Input annotated field must be public, non-final, non-static"));
		render(CmpFieldInputFinalTest.class);
	}

}
