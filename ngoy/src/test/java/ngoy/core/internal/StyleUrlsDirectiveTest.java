package ngoy.core.internal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.CoreMatchers.containsString;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import ngoy.ANgoyTest;
import ngoy.core.Component;
import ngoy.core.NgModule;
import ngoy.core.NgoyException;

public class StyleUrlsDirectiveTest extends ANgoyTest {
	@Component(selector = "a", template = "", styleUrls = { "style_a0.css", "style_a1.css" })
	public static class ACmp {
	}

	@Component(selector = "b", template = "", styleUrls = { "style_b.css" })
	public static class BCmp {
	}

	@Component(selector = "test", template = "<html></html>")
	@NgModule(declarations = { ACmp.class, BCmp.class })
	public static class CmpAddStyleElement {
	}

	@Test
	public void testAddStyleElement() {
		assertThat(render(CmpAddStyleElement.class)).isEqualTo("<html><style type=\"text/css\">a { color: red; }\n" + //
				"a { display: none; }\n" + //
				"b { color: green; }</style></html>");
	}

	//

	@Component(selector = "test", template = "<html><body><style>body: { color: cyan; }</style></body></html>")
	@NgModule(declarations = { ACmp.class, BCmp.class })
	public static class CmpUseExistingStyleElement {
	}

	@Test
	public void testUseExistingStyleElement() {
		assertThat(render(CmpUseExistingStyleElement.class)).isEqualTo("<html><body><style>body: { color: cyan; }\n" + //
				"a { color: red; }\n" + //
				"a { display: none; }\n" + //
				"b { color: green; }</style></body></html>");
	}

	@Rule
	public ExpectedException expectedEx = ExpectedException.none();

	//

	@Component(selector = "test", template = "<html></html>", styleUrls = { "does-not-exist.css" })
	public static class CmpNotFound {
	}

	@Test
	public void testNotFound() {
		expectedEx.expect(NgoyException.class);
		expectedEx.expectMessage(containsString("Style could not be found"));
		expectedEx.expectMessage(containsString(CmpNotFound.class.getName()));
		render(CmpNotFound.class);
	}
}
