package ngoy;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Ignore;
import org.junit.Test;

import jodd.jerry.Jerry;
import jodd.lagarto.LagartoParser;
import jodd.lagarto.Tag;
import jodd.lagarto.dom.Document;
import jodd.lagarto.dom.Element;
import jodd.lagarto.dom.LagartoDOMBuilder;
import jodd.lagarto.dom.LagartoDOMBuilderTagVisitor;
import jodd.lagarto.dom.LagartoDomBuilderConfig;
import jodd.lagarto.dom.Node;
import jodd.lagarto.dom.Node.NodeType;

public class LagartoTest {

	String html = "<html>\n" //
			+ "<body title=\"x\">\n" //
			+ "hello\n" //
			+ "</body>\n" //
			+ "</html>";

	@Test
	public void testJerry() {
		Jerry jerry = Jerry.jerry(html);
		Node[] nodes = jerry.get();
		assertThat(nodes).hasSize(1);
		assertThat(nodes[0].getNodeType()).isEqualTo(NodeType.DOCUMENT);
		Node body = jerry.$("body")
				.first()
				.get()[0];

		assertThat(body.getNodeName()).isEqualTo("body");
	}

	@Test
	@Ignore
	public void test() {

		LagartoParser lagartoParser = new LagartoParser(html);
		lagartoParser.setConfig(new LagartoDomBuilderConfig().setCalculatePosition(true));

		LagartoDOMBuilder domBuilder = new LagartoDOMBuilder();
		LagartoDOMBuilderTagVisitor domBuilderTagVisitor = new LagartoDOMBuilderTagVisitor(domBuilder) {
			@Override
			protected Element createElementNode(Tag tag) {
//				return new ElementWithPosition(domBuilder, super.createElementNode(tag), tag.getPosition());
				return null;
			}
		};

		lagartoParser.parse(domBuilderTagVisitor);

		Document doc = domBuilderTagVisitor.getDocument();
		Node aaaa = doc.getChildElements()[0].getChildNodes()[0];

		aaaa.toString();
	}

}
