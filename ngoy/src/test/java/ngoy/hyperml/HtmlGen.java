package ngoy.hyperml;

import static java.util.Arrays.asList;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import ngoy.internal.parser.template.CodeBuilder;

public class HtmlGen {

	private static final Set<String> KEYWORDS = new HashSet<>(asList("class", "true", "false", "default", "for", "void", "char"));

	static class Item implements Comparable<Item> {
		final String name;
		final String type;

		public Item(String type, String name) {
			this.name = name;
			this.type = type;
		}

		@Override
		public int compareTo(Item o) {
			return name.compareTo(o.name);
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((name == null) ? 0 : name.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			Item other = (Item) obj;
			if (name == null) {
				if (other.name != null)
					return false;
			} else if (!name.equals(other.name))
				return false;
			return true;
		}
	}

	public static void main(String[] args) throws Exception {
		Set<Item> allAttrs = new HashSet<>();
		Set<Item> allElements = new HashSet<>();

		collect("xhtml5.xsd", allAttrs, allElements);
		collect("xhtml1-strict.xsd", allAttrs, allElements);

		List<Item> attrList = new ArrayList<>(allAttrs);
		Collections.sort(attrList);

		List<Item> elementList = new ArrayList<>(allElements);
		Collections.sort(elementList);

		String code = new CodeBuilder() {
			@Override
			protected void doCreate() {
				for (Item it : attrList) {
					String name = it.name;
					if (name.isEmpty() || name.equals("1") || name.equalsIgnoreCase("a") || name.equalsIgnoreCase("i")) {
						continue;
					}
					$("/**");
					$(" * The <code>", name, "</code> ", it.type, ".");
					$(" */");
					$("public static final String ", toName(name), " = \"", name, "\";");
				}

				for (Item a : elementList) {
					String name = a.name;
					if (name.isEmpty()) {
						continue;
					}
					$("/**");
					$(" * The <code>", name, "</code> element.");
					$(" * @return this");
					$(" */");
					$("public Html ", toName(name), "(Object...params) {");
					$("return $(\"", name, "\", params);");
					$("}");
				}
			}
		}.create()
				.toString();

		System.out.println(code);
	}

	private static void collect(String xsd, Set<Item> allAttrs, Set<Item> allElements) throws ParserConfigurationException, SAXException, IOException, XPathExpressionException {
		DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = builderFactory.newDocumentBuilder();
		Document xmlDocument = builder.parse(HtmlGen.class.getResourceAsStream(xsd));

		XPath xPath = XPathFactory.newInstance()
				.newXPath();

		NodeList attrs = (NodeList) xPath.compile("//*[name() = 'xs:attribute']/@name")
				.evaluate(xmlDocument, XPathConstants.NODESET);
		addToSet("attribute", attrs, allAttrs);

		NodeList enums = (NodeList) xPath.compile("//*[name() = 'xs:enumeration']/@value")
				.evaluate(xmlDocument, XPathConstants.NODESET);
		addToSet("enumeration", enums, allAttrs);

		NodeList els = (NodeList) xPath.compile("//*[name() = 'xs:element']/@name")
				.evaluate(xmlDocument, XPathConstants.NODESET);
		addToSet("", els, allElements);
	}

	private static void addToSet(String type, NodeList attrs, Set<Item> allAttrs) {
		for (int i = 0, n = attrs.getLength(); i < n; i++) {
			allAttrs.add(new Item(type, attrs.item(i)
					.getNodeValue()));
		}
	}

	private static String toName(String attr) {
		if (KEYWORDS.contains(attr)) {
			return attr + attr.charAt(attr.length() - 1);
		}
		StringBuilder sb = new StringBuilder();
		boolean nextUpper = false;
		for (char c : attr.toCharArray()) {
			if (Character.isLetterOrDigit(c) || c == '_') {
				sb.append(nextUpper ? Character.toUpperCase(c) : c);
				nextUpper = false;
			} else {
				nextUpper = true;
			}
		}
		return sb.toString();
	}
}
