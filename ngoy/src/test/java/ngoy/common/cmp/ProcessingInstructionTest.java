package ngoy.common.cmp;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;

import org.junit.Test;

import jodd.lagarto.dom.Document;
import jodd.lagarto.dom.LagartoDOMBuilder;
import jodd.lagarto.dom.LagartoDomBuilderConfig;
import jodd.lagarto.dom.Node.NodeType;
import ngoy.ANgoyTest;

public class ProcessingInstructionTest extends ANgoyTest {

	@Test
	public void test() {
		assertThat(render("<!doctype html>\n<html> <a><!-- comment -->x</a> <![CDATA[<xml/>]]> </html>")).isEqualTo("<!DOCTYPE html>\n<html> <a><!-- comment -->x</a> <![CDATA[<xml/>]]> </html>");
	}

	@Test
	public void testCData() {
		LagartoDOMBuilder domBuilder = new LagartoDOMBuilder();

		LagartoDomBuilderConfig config = new LagartoDomBuilderConfig();
		config.setParseXmlTags(true);
		domBuilder.setConfig(config);

		String html = "<html><![CDATA[hello]]></html>";
		Document doc = domBuilder.parse(html);
		assertEquals(NodeType.CDATA, doc.getChild(0)
				.getChild(0)
				.getNodeType());
	}
}
