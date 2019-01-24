package ngoy.hyperml;

import java.io.StringWriter;
import java.io.Writer;

import org.custommonkey.xmlunit.Diff;
import org.custommonkey.xmlunit.XMLTestCase;
import org.custommonkey.xmlunit.XMLUnit;
import org.junit.Rule;
import org.junit.rules.ExpectedException;

/**
 * @author krizzdewizz
 */
abstract public class AbstractXmlTest {

	@Rule
	public ExpectedException expectedEx = ExpectedException.none();

	public static Base.Handler createContentHandler(Writer out) {
		return new Base.WriterHandler(out);
	}

	protected static String createXmlContent(Base<?> xml) {
		StringWriter out = new StringWriter();
		xml.build(out);
		return out.toString();
	}

	public static void myAssertXMLEqual(String expected, String actual) throws Exception {
		boolean oldIgnoreWhitespace = XMLUnit.getIgnoreWhitespace();
		try {
			XMLUnit.setIgnoreWhitespace(true);

			Diff myDiff = new Diff(expected, actual);
			if (!myDiff.similar()) {
				XMLTestCase.assertEquals(myDiff.toString(), expected, actual);
			}
		} finally {
			XMLUnit.setIgnoreAttributeOrder(oldIgnoreWhitespace);
		}
	}

}
