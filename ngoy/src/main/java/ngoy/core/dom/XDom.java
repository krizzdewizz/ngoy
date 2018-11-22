package ngoy.core.dom;

import static java.lang.String.format;
import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;
import static ngoy.core.NgoyException.wrap;
import static ngoy.core.dom.NgoyElement.getPosition;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import jodd.jerry.Jerry;
import jodd.lagarto.dom.Attribute;
import jodd.lagarto.dom.Node;
import ngoy.core.dom.internal.NgoyDomBuilder;

public class XDom {

	public static Jerry parseHtml(String template, int baseLineNumber) {
		try {
			Jerry doc = Jerry.jerry(new NgoyDomBuilder(baseLineNumber))
					.parse(template);
			return doc;
		} catch (Exception e) {
			throw wrap(e);
		}
	}

	public static void traverse(Jerry node, NodeVisitor visitor) {
		accept(node, visitor);
	}

	private static void accept(Jerry nodes, NodeVisitor visitor) {
		nodes.each((n, i) -> {
			visitor.head(n);
			accept(n.contents(), visitor);
			visitor.tail(n);
			return true;
		});
	}

	public static Jerry appendChild(Jerry parent, Jerry el) {
		parent.get(0)
				.addChild(el.get(0));
		return el;
	}

	public static void remove(Node node) {
		node.getParentNode()
				.removeChild(node);
	}

	public static List<String> classNames(Jerry el) {
		return split(el, "class", " ");
	}

	public static List<String> styleNames(Jerry el) {
		return split(el, "style", ";");
	}

	private static List<String> split(Jerry el, String attr, String delimiter) {
		String value = el.attr(attr);
		return value == null //
				? emptyList()
				: Stream.of(value.split(delimiter))
						.map(String::trim)
						.filter(s -> !s.isEmpty())
						.collect(toList());
	}

	public static void removeContents(Jerry el) {
		Stream.of(el.contents()
				.get())
				.forEach(XDom::remove);
	}

	public static boolean matchesAttributeBinding(Jerry node, String attrName) {
		// directive name same as @Input
		String raw = attrName.substring(1, attrName.length() - 1);
		return node.is(format("[\\[%s\\]]", raw));
	}

	public static Jerry cloneNode(Jerry node) {
		return parseHtml(getHtml(node), getPosition(node).getLine()).children()
				.first();
	}

	public static String getHtml(Jerry node) {
		return node.get(0)
				.getHtml();
	}

	public static Jerry createElement(String name, Jerry nodeForBaseLineNumber) {
		return createElement(name, getPosition(nodeForBaseLineNumber).getLine());
	}

	public static Jerry createElement(String name, int baseLineNumber) {
		return parseHtml(format("<%s></%s>", name, name), baseLineNumber).children()
				.first();
	}

	public static String getNodeName(Jerry el) {
		return el.get(0)
				.getNodeName();
	}

	public static List<Attribute> getAttributes(Jerry el) {
		Node ell = el.get(0);
		List<Attribute> all = new ArrayList<>();
		for (int i = 0, n = ell.getAttributesCount(); i < n; i++) {
			all.add(ell.getAttribute(i));
		}
		return all;
	}
}
