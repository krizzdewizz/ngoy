package ngoy.internal.parser;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

import jodd.jerry.Jerry;
import jodd.lagarto.dom.Node;
import jodd.lagarto.dom.Node.NodeType;
import ngoy.internal.parser.NgoyElement;
import ngoy.internal.parser.Parser;
import ngoy.internal.parser.XDom;

public class XDomTest {
	@Test
	public void classNames() {
		assertThat(XDom.classNames(Parser.parseHtml("<a class=\"x   a  s\"></a>", 0)
				.children()
				.first())).isEqualTo(asList("x", "a", "s"));
	}

	@Test
	public void styleNames() {
		assertThat(XDom.styleNames(Parser.parseHtml("<a style=\"color:  red;  white-space: nowrap;x:y\"></a>", 0)
				.children()
				.first())).isEqualTo(asList("color:  red", "white-space: nowrap", "x:y"));
	}

	@Test
	public void createElement() {
		Jerry el = XDom.createElement("x", 10);
		Node ell = el.get(0);
		assertThat(ell.getNodeType()).isEqualTo(NodeType.ELEMENT);
		assertThat(ell.getNodeName()).isEqualTo("x");
		assertThat(NgoyElement.getPosition(el)
				.getLine()).isEqualTo(10);
	}
}
