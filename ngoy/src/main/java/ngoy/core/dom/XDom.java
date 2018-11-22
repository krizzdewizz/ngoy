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

/**
 * DOM utilities.
 * 
 * @author krizz
 */
public class XDom {

	/**
	 * Parse the given html.
	 * <p>
	 * Position information are kept.
	 * 
	 * @param html           HTML to parse
	 * @param baseLineNumber Offset the parsed nodes line by this number, starting a
	 *                       <code>0</code>. When appending a new node, it is
	 *                       important to maintain the new node's line number for
	 *                       better debugging experience
	 * @return Jerry
	 */
	public static Jerry parseHtml(String html, int baseLineNumber) {
		try {
			Jerry doc = Jerry.jerry(new NgoyDomBuilder(baseLineNumber))
					.parse(html);
			return doc;
		} catch (Exception e) {
			throw wrap(e);
		}
	}

	/**
	 * Depth-first accept the given visitor on the nodes.
	 * 
	 * @param nodes
	 * @param visitor
	 */
	public static void accept(Jerry nodes, NodeVisitor visitor) {
		nodes.each((n, i) -> {
			visitor.start(n);
			accept(n.contents(), visitor);
			visitor.end(n);
			return true;
		});
	}

	/**
	 * Append a node.
	 * 
	 * @param parent Node to where to append to
	 * @param el     Node to append
	 * @return The appended node <code>el</code>
	 */
	public static Jerry appendChild(Jerry parent, Jerry el) {
		parent.get(0)
				.addChild(el.get(0));
		return el;
	}

	/**
	 * Removes the given node from its parent.
	 * 
	 * @param node Node to remove
	 */
	public static void remove(Node node) {
		node.getParentNode()
				.removeChild(node);
	}

	/**
	 * Returns a list of all the element's class names.
	 * 
	 * @param el
	 * @return List of class names
	 */
	public static List<String> getClassList(Jerry el) {
		return split(el, "class", " ");
	}

	/**
	 * Returns a list of all the element's styles.
	 * 
	 * @param el
	 * @return List of styles
	 */
	public static List<String> getStyleList(Jerry el) {
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

	/**
	 * Removes al content from the given element.
	 * 
	 * @param el
	 */
	public static void removeContents(Jerry el) {
		Stream.of(el.contents()
				.get())
				.forEach(XDom::remove);
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
