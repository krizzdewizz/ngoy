package ngoy.internal.parser.visitor;

import static java.lang.String.format;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import jodd.jerry.Jerry;
import jodd.lagarto.dom.Attribute;
import jodd.lagarto.dom.Node;
import ngoy.internal.parser.ObjParser;
import ngoy.internal.parser.Parser;

public class XDom {

	public static void traverse(Jerry node, NodeVisitor visitor) {
		accept(node, visitor);
	}

	private static void accept(Jerry nodes, NodeVisitor visitor) {
		nodes.each((n, i) -> {
			visitor.head(n, 0);
			Jerry children = n.contents();
			accept(children, visitor);
			visitor.tail(n, 0);
			return true;
		});
	}

	public static void appendChild(Jerry parent, Jerry ell) {
		parent.get(0)
				.addChild(ell.get(0));
	}

	public static void remove(Node node) {
		node.getParentNode()
				.removeChild(node);
	}

	public static List<String> classNames(Jerry el) {
		String clazz = el.attr("class");
		return clazz == null ? emptyList() : asList(clazz.split(" "));
	}

	public static List<String> styleNames(Jerry el) {
		List<String> all = new ArrayList<>();
		String style = el.attr("style");
		if (style != null) {
			Map<String, String> map = ObjParser.parse(format("{%s}", style));
			for (Map.Entry<String, String> entry : map.entrySet()) {
				all.add(format("%s:%s", entry.getKey(), entry.getValue()));
			}
		}
		return all;
	}

	public static void removeContents(Jerry el) {
		Stream.of(el.contents()
				.get())
				.forEach(XDom::remove);
	}

	public static boolean matchesAttributeBinding(Jerry node, String attrName) {
//		// directive name same as @Input
		String raw = attrName.substring(1, attrName.length() - 1);
		return node.is(format("[\\[%s\\]]", raw));
	}

	public static Jerry cloneNode(Jerry node) {
		return Parser.parseHtml(getHtml(node));
	}

	public static String getHtml(Jerry node) {
		return node.get(0)
				.getHtml();
	}

	public static Jerry createElement(String name) {
		return Parser.parseHtml(format("<%s></%s>", name, name))
				.children()
				.first();
	}

	public static String nodeName(Jerry el) {
		return el.get(0)
				.getNodeName();
	}

	public static List<Attribute> attributes(Jerry el) {
		Node ell = el.get(0);
		List<Attribute> all = new ArrayList<>();
		for (int i = 0, n = ell.getAttributesCount(); i < n; i++) {
			all.add(ell.getAttribute(i));
		}
		return all;
	}
}
