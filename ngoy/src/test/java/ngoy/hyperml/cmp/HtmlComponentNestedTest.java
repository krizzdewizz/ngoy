package ngoy.hyperml.cmp;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

import ngoy.ANgoyTest;
import ngoy.core.Component;
import ngoy.core.Input;
import ngoy.core.NgModule;
import ngoy.hyperml.AHtmlComponent;

public class HtmlComponentNestedTest extends ANgoyTest {

	@Component(selector = "", template = "<html><x></x></html>")
	@NgModule(declarations = { XCmp.class, YCmp.class })
	public static class TestAppCmp {
	}

	@Component(selector = "y")
	public static class YCmp extends AHtmlComponent {

		@Input
		public String title;

		private int age;

		@Input
		public void setAge(int age) {
			this.age = age;
		}

		@Override
		protected void template() {
			text("M:", title, age == 0 ? null : age);
		}
	}

	@Component(selector = "x")
	public static class XCmp extends AHtmlComponent {
		@Override
		protected void template() {
			div();
			{
				$(YCmp.class, $);
				$("y", $);

				this.<YCmp>$("y", y -> y.title = "qbert", $);
				$("y", y -> ((YCmp) y).title = "sky", $);
				$(YCmp.class, y -> y.title = "hello", $);
			}
			$(); // div
		}
	}

	@Test
	public void testApp() {
		assertThat(render(TestAppCmp.class)).isEqualTo("<html><x><div><y>M:</y><y>M:</y><y>M:qbert</y><y>M:sky</y><y>M:hello</y></div></x></html>");
	}

	//

	@Component(selector = "", template = "<q></q>")
	@NgModule(declarations = { XInitWithAttrsCmp.class, YCmp.class })
	public static class TestApp2Cmp {
	}

	@Component(selector = "q")
	public static class XInitWithAttrsCmp extends AHtmlComponent {
		@Override
		protected void template() {
			div();
			{
				$(YCmp.class, "title", "hello", "age", 21, $);
			}
			$(); // div
		}
	}

	@Test
	public void testApp2() {
		assertThat(render(TestApp2Cmp.class)).isEqualTo("<q><div><y>M:hello21</y></div></q>");
	}
}
