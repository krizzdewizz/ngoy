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
			/* @formatter:off */
			protected void create() {
				$("div", classs, "title");
				{
					for (int i : ints) {
						$("span").text("hello:", i, $);
					}
					text("<>");
				}
				$(); // div
			}
			/* @formatter:on */
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
}
