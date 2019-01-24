package ngoy.common.cmp;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

import ngoy.ANgoyTest;
import ngoy.common.AHtmlComponent;
import ngoy.core.Component;
import ngoy.core.OnRender;
import ngoy.core.Output;
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
		public void ngOnRender(Output output) {
			html.build(output);
		}
	}

	@Test
	public void test() {
		assertThat(render(TestCmp.class)).isEqualTo("<div class=\"title\"><span>hello:1</span><span>hello:2</span><span>hello:3</span>&lt;&gt;</div>abc");
	}

	//

	@Component(selector = "", template = "abc")
	public static class TestSubclassCmp extends AHtmlComponent {

		private int[] ints = new int[] { 1, 2, 3 };

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
	}

	@Test
	public void testSubclass() {
		assertThat(render(TestSubclassCmp.class)).isEqualTo("<div class=\"title\"><span>hello:1</span><span>hello:2</span><span>hello:3</span>&lt;&gt;</div>abc");
	}
}
