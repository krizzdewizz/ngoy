package ngoy.hyperml.cmp;

import static org.hamcrest.CoreMatchers.containsString;

import org.junit.Test;

import ngoy.ANgoyTest;
import ngoy.core.Component;
import ngoy.core.Input;
import ngoy.core.NgModule;
import ngoy.core.NgoyException;
import ngoy.core.OnRender;
import ngoy.hyperml.HtmlComponent;

public class ErrorTest extends ANgoyTest {

	@Component(selector = "x")
	public static class NonOnRenderCmp {
	}

	@Component(selector = "")
	@NgModule(declarations = { NonOnRenderCmp.class })
	public static class TestAppCmp extends HtmlComponent {

		@Override
		protected void template() {
			$("x", $);
		}
	}

	@Test
	public void errorNotOnRender() {
		expectedEx.expect(NgoyException.class);
		expectedEx.expectMessage(containsString(String.format("must be an instance of %s", OnRender.class.getName())));
		expectedEx.expectMessage(containsString(NonOnRenderCmp.class.getName()));
		render(TestAppCmp.class);
	}

	//

	@Component(selector = "x")
	public static class WrongInputCmp extends HtmlComponent {
		@Input
		public int age;

		@Override
		protected void template() {
		}
	}

	@Component(selector = "")
	@NgModule(declarations = { WrongInputCmp.class })
	public static class TestAppWrongInputTypeCmp extends HtmlComponent {
		@Override
		protected void template() {
			$("x", "age", "not-a-int", $);
		}
	}

	@Test
	public void errorWrongInputType() {
		expectedEx.expect(NgoyException.class);
		expectedEx.expectMessage(containsString("Error while setting component input"));
		expectedEx.expectMessage(containsString(WrongInputCmp.class.getName()));
		expectedEx.expectMessage(containsString("age"));
		render(TestAppWrongInputTypeCmp.class);
	}

	//

	@Component(selector = "")
	public static class TestAppNullAttributeNameCmp extends HtmlComponent {
		@Override
		protected void template() {
			span(null, "value", $);
		}
	}

	@Test
	public void nullAttributeName() {
		expectedEx.expect(NgoyException.class);
		expectedEx.expectMessage(containsString("attribute name must not be null"));
		render(TestAppNullAttributeNameCmp.class);
	}

	//

	@Component(selector = "")
	public static class TestAppEmptyAttributeNameCmp extends HtmlComponent {
		@Override
		protected void template() {
			span("", "value", $);
		}
	}

	@Test
	public void emptyAttributeName() {
		expectedEx.expect(NgoyException.class);
		expectedEx.expectMessage(containsString("attribute name must not be empty"));
		render(TestAppEmptyAttributeNameCmp.class);
	}
}
