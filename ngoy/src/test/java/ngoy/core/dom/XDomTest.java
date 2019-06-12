package ngoy.core.dom;

import jodd.jerry.Jerry;
import jodd.lagarto.dom.Node;
import jodd.lagarto.dom.Node.NodeType;
import org.junit.Test;

import static java.util.Arrays.asList;
import static ngoy.core.dom.XDom.parseHtml;
import static org.assertj.core.api.Assertions.assertThat;

public class XDomTest {
	@Test
	public void classNames() {
		assertThat(XDom.getClassList(parseHtml("<a class=\"x   a  s\"></a>", 0).children()
				.first())).isEqualTo(asList("x", "a", "s"));
	}

	@Test
	public void styleNames() {
		assertThat(XDom.getStyleList(parseHtml("<a style=\"color:  red;  white-space: nowrap;x:y\"></a>", 0).children()
				.first())).isEqualTo(asList("color:  red", "white-space: nowrap", "x:y"));
	}

	@Test
	public void createElement() {
		Jerry el = XDom.createElement("x", 10);
		Node ell = el.get(0);
		assertThat(ell.getNodeType()).isEqualTo(NodeType.ELEMENT);
		assertThat(ell.getNodeName()).isEqualTo("x");
		assertThat(NgoyElement.getPosition(el)
				.getLine()).isEqualTo(11);
	}
}
