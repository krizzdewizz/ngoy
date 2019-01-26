package ngoy.hyperml;

import java.io.StringWriter;

import org.custommonkey.xmlunit.Diff;
import org.custommonkey.xmlunit.XMLUnit;
import org.junit.Rule;
import org.junit.rules.ExpectedException;

import junit.framework.TestCase;
import ngoy.hyperml.base.BaseMl;

/**
 * @author krizzdewizz
 */
abstract public class AbstractXmlTest {

	@Rule
	public ExpectedException expectedEx = ExpectedException.none();

	protected static String createXmlContent(BaseMl<?> xml) {
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
				TestCase.assertEquals(myDiff.toString(), expected, actual);
			}
		} finally {
			XMLUnit.setIgnoreAttributeOrder(oldIgnoreWhitespace);
		}
	}

}
