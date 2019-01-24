package ngoy.hyperml;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.CoreMatchers.containsString;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import ngoy.core.NgoyException;

/**
 * @author krizz
 */
public class HtmlTest extends AbstractXmlTest {

	@Test
	public void testDoc() throws Exception {
		Html xml = new Html() {
			protected void create() {
				html();
				{
					body(onload, "alert('kuckuck')");
					{
						h3("hello", $);
					}
					$();
				}
				$();
			}
		};

		myAssertXMLEqual("<html><body onload=\"alert('kuckuck')\"><h3>hello</h3></body></html>", createXmlContent(xml));
	}

	@Test
	public void noEscapeInScriptAndStyle() throws Exception {
		Html xml = new Html() {
			protected void create() {
				html();
				{
					text("<>");
					style();
					{
						text("a > * { color: red }");
					}
					$();

					script();
					{
						text("let a = 0 > 1;");
					}
					$();
				}
				$();
			}
		};

		myAssertXMLEqual("<html>&lt;&gt;<style>a > * { color: red }</style><script>let a = 0 > 1;</script></html>", createXmlContent(xml));
	}

	@Test
	public void noEndForVoidElements() throws Exception {
		Html xml = new Html() {
			protected void create() {
				html();
				{
					body();
					{
						input(value, "hello");
					}
					$();
				}
				$();
			}
		};

		assertThat(createXmlContent(xml)).isEqualTo("<html><body><input value=\"hello\"></body></html>");
	}

	@Test
	public void noEndForVoidElementsCheck() throws Exception {
		Html xml = new Html() {
			protected void create() {
				html();
				{
					body();
					{
						input(value, "hello", $);
					}
					$();
				}
				$();
			}
		};

		expectedEx.expect(NgoyException.class);
		expectedEx.expectMessage(containsString("void elements must not be ended: input"));

		createXmlContent(xml);
	}

	@Test
	public void classes() throws Exception {
		Html xml = new Html() {
			protected void create() {
				a(classs, classes("peter", true, "paul", false, "mary", Boolean.TRUE), $);
			}
		};

		myAssertXMLEqual("<a class='peter mary'></a>", createXmlContent(xml));
	}

	@Test
	public void classesMap() throws Exception {
		Html xml = new Html() {
			protected void create() {
				Map<String, Boolean> classes = new HashMap<>();
				classes.put("peter", true);
				classes.put("paul", false);
				classes.put("mary", Boolean.TRUE);

				a(classs, classes(classes), $);
			}
		};

		myAssertXMLEqual("<a class='peter mary'></a>", createXmlContent(xml));
	}

	@Test
	public void styles() throws Exception {
		Html xml = new Html() {
			protected void create() {
				a(style, styles("background-color", "red", "color", null, "display", "none"), $);
			}
		};

		myAssertXMLEqual("<a style='background-color:red;display:none'></a>", createXmlContent(xml));
	}

	@Test
	public void stylesMap() throws Exception {
		Html xml = new Html() {
			protected void create() {
				Map<String, String> styles = new HashMap<>();
				styles.put("background-color", "red");
				styles.put("color", null);
				styles.put("display", "none");

				a(style, styles(styles), $);
			}
		};

		myAssertXMLEqual("<a style='background-color:red;display:none'></a>", createXmlContent(xml));
	}
}
