package ngoy.hyperml.cmp;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

import ngoy.ANgoyTest;
import ngoy.core.Component;
import ngoy.core.NgModule;
import ngoy.hyperml.HtmlComponent;

public class NestedContentTest extends ANgoyTest {
	@Component(selector = "x")
	public static class XCmp extends HtmlComponent {

		public Runnable headerContent;
		public Runnable footerContent;

		public void init(Runnable headerContent, Runnable footerContent) {
			this.headerContent = headerContent;
			this.footerContent = footerContent;
		}

		@Override
		protected void template() {
			$("header");
			{
				if (headerContent != null) {
					headerContent.run();
				}
			}
			$();

			div("hello", $);

			$("footer");
			{
				if (footerContent != null) {
					footerContent.run();
				}
			}
			$();
		}
	}

	@Component(selector = "")
	@NgModule(declarations = { XCmp.class })
	public static class TestAppCmp extends HtmlComponent {

		private void myHeader() {
			text("my header");
		}

		private void myFooter() {
			text("my footer");
		}

		@Override
		protected void template() {
			$(XCmp.class, x -> x.init(this::myHeader, this::myFooter), $);
		}
	}

	@Test
	public void testApp() {
		assertThat(render(TestAppCmp.class)).isEqualTo("<x><header>my header</header><div>hello</div><footer>my footer</footer></x>");
	}
}
