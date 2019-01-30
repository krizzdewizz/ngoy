package ngoy.hyperml;

import static java.util.Arrays.asList;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import ngoy.internal.parser.template.CodeBuilder;

public class HtmlGen {

	private static final Set<String> KEYWORDS = new HashSet<>(asList("class", "true", "false", "default", "for", "void", "char", "continue", "float", "map"));
	private static final String[] UNITS = new String[] { "em", "ex", "percent", "px", "cm", "mm", "in", "pt", "pc", "ch", "rem", "vh", "vwv", "vmin", "vmax" };

	static class Item implements Comparable<Item> {
		final String name;
		final String type;
		final String value;

		public Item(String type, String name, String value) {
			this.name = name;
			this.type = type;
			this.value = value;
		}

		public Item(String type, String name) {
			this(type, name, name);
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

	interface Collect {
		void run(Set<Item> allAttrs, Set<Item> allElements) throws Exception;
	}

	public static void main(String[] args) throws Exception {
		generate("src/test/java/ngoy/hyperml/xxhtml.txt", (allAttrs, allElements) -> {
			collect("xhtml5.xsd", allAttrs, allElements);
			collect("xhtml1-strict.xsd", allAttrs, allElements);
			collectCssProps(allAttrs);
			collectUnits(allAttrs);
		});

//		generate("src/test/java/ngoy/hyperml/xxcss.txt", true, (allAttrs, allElements) -> {
//			collectCssProps(allAttrs);
//		});
	}

	private static void collectUnits(Set<Item> allAttrs) {
		for (String unit : UNITS) {
			allAttrs.add(new Item("unit", unit, unit.equals("percent") ? "%" : unit));
		}
	}

	private static void generate(String targetFile, Collect collect) throws Exception {
		Set<Item> allAttrs = new HashSet<>();
		Set<Item> allElements = new HashSet<>();

		collect.run(allAttrs, allElements);

		List<Item> attrList = new ArrayList<>(allAttrs);
		Collections.sort(attrList);

		List<Item> elementList = new ArrayList<>(allElements);
		Collections.sort(elementList);

		String code = new CodeBuilder() {
			@Override
			protected void doCreate() {
				for (Item it : attrList) {
					String name = it.name;
					if (exclude(name)) {
						continue;
					}
					$("/**");
					$(" * The <code>", name, "</code> ", it.type, ".");
					$(" */");

					$$("public static final ");

					if (it.type.equals("unit")) {
						$("Unit ", toName(name), " = new Unit(\"", it.value, "\");");
					} else {
						$("String ", toName(name), " = \"", it.value, "\";");
					}
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
					$("public T ", toName(name), "(Object...params) {");
					$("return $(\"", name, "\", params);");
					$("}");
				}
			}
		}.create()
				.toString();

		Files.write(Paths.get(targetFile), code.getBytes());
	}

	private static void collectCssProps(Set<Item> allAttrs) throws Exception {
		Document xmlDocument = parseXml("all-properties.en.xml");

		XPath xPath = XPathFactory.newInstance()
				.newXPath();

		NodeList props = (NodeList) xPath.compile("//record[f[@name='status']='REC' or f[@name='status']='NOTE']/f[@name='property']")
				.evaluate(xmlDocument, XPathConstants.NODESET);

//		System.err.println("sss: " + props.getLength());
		addToSet("css property", props, allAttrs);
	}

	private static void collect(String xsd, Set<Item> allAttrs, Set<Item> allElements) throws Exception {
		Document xmlDocument = parseXml(xsd);

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

	private static Document parseXml(String resource) throws Exception {
		DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = builderFactory.newDocumentBuilder();
		return builder.parse(HtmlGen.class.getResourceAsStream(resource));
	}

	private static void addToSet(String type, NodeList attrs, Set<Item> allAttrs) {
		for (int i = 0, n = attrs.getLength(); i < n; i++) {
			allAttrs.add(new Item(type, attrs.item(i)
					.getTextContent()));
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

	private static boolean exclude(String name) {
		return name.isEmpty() || name.equals("1") || name.equalsIgnoreCase("a") || name.equalsIgnoreCase("i") || name.contains("*");
	}
}
