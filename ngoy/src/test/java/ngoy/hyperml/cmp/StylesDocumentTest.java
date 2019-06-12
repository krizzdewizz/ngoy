package ngoy.hyperml.cmp;

import hyperml.base.BaseMl;
import ngoy.ANgoyTest;
import ngoy.core.Component;
import ngoy.core.NgModule;
import ngoy.hyperml.Html;
import ngoy.hyperml.HtmlComponent;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class StylesDocumentTest extends ANgoyTest {

	public static class CssDoc extends Html {
		@Override
		protected void create() {
			css("a", color, "red");
		}
	}

	@Component(selector = "x")
	public static class XCmp extends HtmlComponent {

		@Override
		protected BaseMl<?> stylesDocument() {
			return new CssDoc();
		}

		@Override
		protected void content() {
			text("X");
		}
	}

	@Component(selector = "", template = "<html><x></x></html>")
	@NgModule(declarations = { XCmp.class })
	public static class TestAppCmp {
	}

	@Test
	public void testApp() {
		assertThat(render(TestAppCmp.class)).isEqualTo("<html><style type=\"text/css\">a{color:red;}</style><x>X</x></html>");
	}

	//

	@Component(selector = "")
	public static class TestApp2Cmp extends HtmlComponent {

		@Override
		protected BaseMl<?> stylesDocument() {
			return new CssDoc();
		}

		@Override
		protected void content() {
			style();
			{
				text(stylesDocument());
			}
			$();
		}
	}

	@Test
	public void testApp2() {
		assertThat(render(TestApp2Cmp.class)).isEqualTo("<style>a{color:red;}</style>");
	}
}
