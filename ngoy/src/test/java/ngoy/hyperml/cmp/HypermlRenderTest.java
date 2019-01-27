package ngoy.hyperml.cmp;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

import ngoy.ANgoyTest;
import ngoy.core.Component;
import ngoy.core.NgModule;
import ngoy.core.OnRender;
import ngoy.core.Output;
import ngoy.hyperml.HtmlComponent;
import ngoy.hyperml.Html;

public class HypermlRenderTest extends ANgoyTest {

	@Component(selector = "", template = "abc")
	public static class TestCmp implements OnRender {

		private int[] ints = new int[] { 1, 2, 3 };

		private Html html = new Html() {
			@Override
			protected void create() {
				div(classs, "title");
				{
					for (int i : ints) {
						span().text("hello:", i, $);
					}
					text("<>");
				}
				$(); // div
			}
		};

		@Override
		public void onRender(Output output) {
			html.build(output);
		}
	}

	@Test
	public void test() {
		assertThat(render(TestCmp.class)).isEqualTo("<div class=\"title\"><span>hello:1</span><span>hello:2</span><span>hello:3</span>&lt;&gt;</div>abc");
	}

	//

	@Component(selector = "", template = "<html><head></head><x></x></html>")
	@NgModule(declarations = { TestSubclassCmp.class })
	public static class TestSubclassAppCmp {
	}

	@Component(selector = "x")
	public static class TestSubclassCmp extends HtmlComponent {

		private int[] ints = new int[] { 1, 2, 3 };

		@Override
		protected void styles() {
			css("a", color, "red");
			css("body");
			{
				$(backgroundColor, "blue");
			}
			$();
		}

		@Override
		protected void content() {
			div(classs, "title");
			{
				for (int i : ints) {
					span().text("hello:", i, $);
				}
				text("<>");
			}
			$(); // div
		}
	}

	@Test
	public void testSubclass() {
		assertThat(render(TestSubclassAppCmp.class)).isEqualTo(
				"<html><head><style type=\"text/css\">a{color:red;}body{background-color:blue;}</style></head><x><div class=\"title\"><span>hello:1</span><span>hello:2</span><span>hello:3</span>&lt;&gt;</div></x></html>");
	}
}
