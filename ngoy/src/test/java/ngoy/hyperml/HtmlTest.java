package ngoy.hyperml;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.CoreMatchers.containsString;

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
}
