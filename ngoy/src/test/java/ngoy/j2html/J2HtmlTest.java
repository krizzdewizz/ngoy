package ngoy.j2html;

import static j2html.TagCreator.div;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

import j2html.tags.DomContent;
import j2html.tags.Text;
import ngoy.ANgoyTest;
import ngoy.core.Component;
import ngoy.core.HostBinding;
import ngoy.core.NgModule;
import ngoy.core.OnInit;

public class J2HtmlTest extends ANgoyTest {
	@Component(selector = "x")
	public static class XCmp extends J2HtmlComponent implements OnInit {

		@HostBinding("attr.title")
		public String title;

		public int age;

		@HostBinding("class.cool")
		public boolean cool = true;

		@HostBinding("style.color")
		public String color = "red";

		@Override
		public void onInit() {
			title = "hellox";
		}

		@Override
		protected DomContent content() {
			return new Text(String.valueOf(age));
		}
	}

	@Component(selector = "")
	@NgModule(declarations = { XCmp.class })
	public static class TestAppCmp extends J2HtmlComponent {
		@Override
		protected DomContent content() {
			return div(cmp(XCmp.class, c -> c.age = 21).attr("q", "more"));
		}
	}

	@Test
	public void testApp() {
		assertThat(render(TestAppCmp.class)).isEqualTo("<div><x class=\"cool\" style=\"color:red\" title=\"hellox\" q=\"more\">21</x></div>");
	}
}
