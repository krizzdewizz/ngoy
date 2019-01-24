package ngoy.core.internal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.CoreMatchers.containsString;

import org.junit.Test;

import ngoy.ANgoyTest;
import ngoy.core.Component;
import ngoy.core.NgModule;
import ngoy.core.NgoyException;

public class StyleUrlsDirectiveTest extends ANgoyTest {
	@Component(selector = "a-cmp", template = "", styleUrls = { "style_a0.css", "style_a1.css" })
	public static class ACmp {
	}

	@Component(selector = "b-cmp", template = "", styleUrls = { "style_b.css" })
	public static class BCmp {
	}

	@Component(selector = "test", template = "<html></html>")
	@NgModule(declarations = { ACmp.class, BCmp.class })
	public static class CmpAddStyleElement {
	}

	@Test
	public void testAddStyleElement() {
		assertThat(render(CmpAddStyleElement.class, builder -> builder.prefixCss(true))).isEqualTo("<html><style type=\"text/css\">a-cmp a { color: red; }\n" + //
				"a-cmp a { display: none; }\n" + //
				"b-cmp b { color: green; }</style></html>");
	}

	//

	@Component(selector = "test", template = "<html><body><style>body: { color: cyan; }</style></body></html>")
	@NgModule(declarations = { ACmp.class, BCmp.class })
	public static class CmpUseExistingStyleElement {
	}

	//

	@Component(selector = "test", template = "<html></html>", styleUrls = { "does-not-exist.css" })
	public static class CmpNotFound {
	}

	@Test
	public void testNotFound() {
		expectedEx.expect(NgoyException.class);
		expectedEx.expectMessage(containsString("Style resource could not be found"));
		expectedEx.expectMessage(containsString(CmpNotFound.class.getName()));
		render(CmpNotFound.class);
	}

	//

	@Component(selector = "test", template = "<html></html>", styles = { "h1 { font-weight: normal; }" })
	public static class InlineCmp {
	}

	@Test
	public void testInline() {
		assertThat(render(InlineCmp.class)).isEqualTo("<html><style type=\"text/css\">h1 { font-weight: normal; }</style></html>");
	}

	//

	@Component(selector = "test", template = "<html></html>", styleUrls = { "lots-of.css" })
	public static class LotsOfCssCmp {
	}

	@Test
	public void testLotsOfCss() {
		assertThat(flatten(render(LotsOfCssCmp.class, builder -> builder.prefixCss(true)))).isEqualTo(flatten("<html><style type=\"text/css\">test h1 {\n" + //
				"	color: #369;\n" + //
				"	font-family: Arial, Helvetica, sans-serif;\n" + //
				"	font-size: 250%;\n" + //
				"}\n" + //
				"\n" + //
				"test h2, h3 {\n" + //
				"	color: #444;\n" + //
				"	font-family: Arial, Helvetica, sans-serif;\n" + //
				"	font-weight: lighter;\n" + //
				"}\n" + //
				"\n" + //
				"test body {\n" + //
				"	margin: 2em;\n" + //
				"}\n" + //
				"\n" + //
				"test body, input[type=\"text\"], button {\n" + //
				"	color: #888;\n" + //
				"	font-family: Cambria, Georgia;\n" + //
				"}\n" + //
				"\n" + //
				"test nav {\n" + //
				"	margin-bottom: 2rem;\n" + //
				"}\n" + //
				"\n" + //
				"test nav a {\n" + //
				"	padding: 5px;\n" + //
				"}\n" + //
				"\n" + //
				"test nav a.active {\n" + //
				"	color: white;\n" + //
				"	background-color: darkkhaki;\n" + //
				"}\n" + //
				"\n" + //
				"test .error {\n" + //
				"	color: red;\n" + //
				"}\n" + //
				"\n" + //
				"test .info {\n" + //
				"	color: blue;\n" + //
				"}</style></html>"));
	}

	private String flatten(String s) {
		return s.replace("\n", "")
				.replace("\r", "");
	}

}
