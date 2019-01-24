package ngoy.common.cmp;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

import hyperxml.Html;
import ngoy.ANgoyTest;
import ngoy.core.Component;
import ngoy.core.OnRender;
import ngoy.core.Output;

public class HyperXmlRenderTest extends ANgoyTest {

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
			html.build(output.getWriter());
		}
	}

	@Test
	public void test() {
		assertThat(render(TestCmp.class)).isEqualTo("<div class=\"title\"><span>hello:1</span><span>hello:2</span><span>hello:3</span>&lt;&gt;</div>abc");
	}

	//

	@Component(selector = "", template = "abc")
	public static class TestSubclassCmp extends Html implements OnRender {

		private int[] ints = new int[] { 1, 2, 3 };

		@Override
		public void ngOnRender(Output output) {
			build(output.getWriter());
		}

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
