package ngoy.hyperml;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.Assert.assertEquals;

import java.util.LinkedHashMap;
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
				Map<String, Boolean> classes = new LinkedHashMap<>();
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
				Map<String, Object> styles = new LinkedHashMap<>();
				styles.put("background-color", "red");
				styles.put("color", null);
				styles.put("display", "none");

				a(style, styles(styles), $);
			}
		};

		myAssertXMLEqual("<a style='background-color:red;display:none'></a>", createXmlContent(xml));
	}

	@Test
	public void testEmptyClassList() throws Exception {
		Html xml = new Html() {
			protected void create() {
				$("content", classs, classes("a", false), $);
			}
		};

		String result = createXmlContent(xml);
		myAssertXMLEqual("<content/>", result);
	}

	@Test
	public void testEmptyStyleList() throws Exception {
		Html xml = new Html() {
			protected void create() {
				$("content", style, styles("color", ""), $);
			}
		};

		String result = createXmlContent(xml);
		myAssertXMLEqual("<content/>", result);
	}

	@Test
	public void testWithUnit() throws Exception {
		Html xml = new Html() {
			protected void create() {
				a(style, styles("height.px", 20, "font-size.rem", 12), $);
			}
		};

		String result = createXmlContent(xml);
		myAssertXMLEqual("<a style=\"height:20px;font-size:12rem\"></a>", result);
	}

	@Test
	public void testWithUnitInstance() throws Exception {
		Html xml = new Html() {
			protected void create() {
				a(style, styles("height", 20, px, "font-size", 12, rem), $);
			}
		};

		String result = createXmlContent(xml);
		myAssertXMLEqual("<a style=\"height:20px;font-size:12rem\"></a>", result);
	}

	@Test
	public void testWithUnitMap() throws Exception {
		Html xml = new Html() {
			protected void create() {
				Map<String, Object> styles = new LinkedHashMap<>();
				styles.put("height.px", 20);
				styles.put("font-size.rem", 12);

				a(style, styles(styles), $);
			}
		};

		String result = createXmlContent(xml);
		myAssertXMLEqual("<a style=\"height:20px;font-size:12rem\"></a>", result);
	}

	@Test
	public void testCssOddLength() throws Exception {
		Html xml = new Html() {
			protected void create() {
				css("body", color);
			}
		};

		expectedEx.expect(NgoyException.class);
		expectedEx.expectMessage(containsString("must be even"));

		createXmlContent(xml);
	}

	@Test
	public void testCssOddLength2() throws Exception {
		Html xml = new Html() {
			protected void create() {
				css("body", color, "red", display);
			}
		};

		expectedEx.expect(NgoyException.class);
		expectedEx.expectMessage(containsString("must be even"));

		createXmlContent(xml);
	}

	@Test
	public void testUnit() throws Exception {
		Html xml = new Html() {
			protected void create() {
				css(".small", width, 12, px);
			}
		};

		String result = createXmlContent(xml);
		assertEquals(".small{width:12px;}", result);
	}

	@Test
	public void testUnit2() throws Exception {
		Html xml = new Html() {
			protected void create() {
				css(".small", color, "red", width, 12, px, display, none);
			}
		};

		String result = createXmlContent(xml);
		assertEquals(".small{color:red;width:12px;display:none;}", result);
	}

	@Test
	public void testUnit3() throws Exception {
		Html xml = new Html() {
			protected void create() {
				css(".small", width, 12, px, height, 13, em, display, none);
			}
		};

		String result = createXmlContent(xml);
		assertEquals(".small{width:12px;height:13em;display:none;}", result);
	}

	@Test
	public void testCss() throws Exception {
		Html xml = new Html() {
			protected void create() {
				css("body", color, "red");
				css("a", color, "green", display, none);
				css(".small", width, 12, px);
			}
		};

		String result = createXmlContent(xml);
		assertEquals("body{color:red;}a{color:green;display:none;}.small{width:12px;}", result);
	}

	@Test
	public void testCssBlock() throws Exception {
		Html xml = new Html() {
			protected void create() {
				css("body");
				{
					$(color, "red");
					$(backgroundColor, "cyan", disabled, falsee);
					$(width, 1, percent);
				}
				$();

				css("body a", color, "red");

				css("a");
				{
					$(whiteSpace, "nowrap");
				}
				$();
			}
		};

		String result = createXmlContent(xml);
		assertEquals("body{color:red;background-color:cyan;disabled:false;width:1%;}body a{color:red;}a{white-space:nowrap;}", result);
	}

	@Test
	public void testCssBlockNestedInvalid() throws Exception {
		Html xml = new Html() {
			protected void create() {
				css("");
				css("");
			}
		};

		expectedEx.expect(NgoyException.class);
		expectedEx.expectMessage(containsString("Nested css() calls are not allowed"));

		createXmlContent(xml);
	}
}
