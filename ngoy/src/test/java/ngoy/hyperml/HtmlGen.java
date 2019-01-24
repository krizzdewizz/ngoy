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

	public static void main(String[] args) throws Exception {
		Set<String> allAttrs = new HashSet<>();
		Set<String> allElements = new HashSet<>();

		collect("xhtml5.xsd", allAttrs, allElements);
		collect("xhtml1-strict.xsd", allAttrs, allElements);

		List<String> attrList = new ArrayList<>(allAttrs);
		Collections.sort(attrList);

		List<String> elementList = new ArrayList<>(allElements);
		Collections.sort(elementList);

		String code = new CodeBuilder() {
			@Override
			protected void doCreate() {
				for (String attr : attrList) {
					if (attr.isEmpty() || attr.equals("1") || attr.equalsIgnoreCase("a") || attr.equalsIgnoreCase("i")) {
						continue;
					}
					$("/**");
					$(" * The <code>", attr, "</code> attribute.");
					$(" */");
					$("public static final String ", toName(attr), " = \"", attr, "\";");
				}

				for (String el : elementList) {
					if (el.isEmpty()) {
						continue;
					}
					$("/**");
					$(" * The <code>", el, "</code> element.");
					$(" * @return this");
					$(" */");
					$("public Html ", toName(el), "(Object...params) {");
					$("return $(\"", el, "\", params);");
					$("}");
				}
			}
		}.create()
				.toString();

		System.out.println(code);
	}

	private static void collect(String xsd, Set<String> allAttrs, Set<String> allElements) throws ParserConfigurationException, SAXException, IOException, XPathExpressionException {
		DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = builderFactory.newDocumentBuilder();
		Document xmlDocument = builder.parse(HtmlGen.class.getResourceAsStream(xsd));

		XPath xPath = XPathFactory.newInstance()
				.newXPath();

		NodeList attrs = (NodeList) xPath.compile("//*[name() = 'xs:attribute']/@name")
				.evaluate(xmlDocument, XPathConstants.NODESET);
		addToSet(attrs, allAttrs);

		NodeList enums = (NodeList) xPath.compile("//*[name() = 'xs:enumeration']/@value")
				.evaluate(xmlDocument, XPathConstants.NODESET);
		addToSet(enums, allAttrs);

		NodeList els = (NodeList) xPath.compile("//*[name() = 'xs:element']/@name")
				.evaluate(xmlDocument, XPathConstants.NODESET);
		addToSet(els, allElements);
	}

	private static void addToSet(NodeList attrs, Set<String> allAttrs) {
		for (int i = 0, n = attrs.getLength(); i < n; i++) {
			allAttrs.add(attrs.item(i)
					.getNodeValue());
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
