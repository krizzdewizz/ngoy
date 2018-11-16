package org.ngoy.internal.parser.visitor;

import static java.lang.String.format;
import static java.util.stream.Collectors.toList;
import static org.ngoy.internal.parser.Parser.NG_TEMPLATE;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.select.NodeVisitor;
import org.ngoy.core.NgoyException;
import org.ngoy.internal.parser.ForOfVariable;
import org.ngoy.internal.parser.ParseException;

public class MicroSyntaxVisitor extends DefaultVisitor {

	private static final Pattern FOR_OF_PATTERN = Pattern.compile("let\\s*(.*)\\s*of\\s*(.*)");
	private static final Pattern VAR_DECL_PATTERN = Pattern.compile("(.*)\\s*as\\s*(.*)");

	private final NodeVisitor src;

	public MicroSyntaxVisitor(NodeVisitor src) {
		this.src = src;
	}

	@Override
	public void head(Node node, int depth) {
		if (node instanceof Element) {
			Element el = (Element) node;
			replaceNgIf(el);
			replaceNgFor(el);
			replaceSwitchCase(el);
			replaceSwitchDefault(el);
		}

		src.head(node, depth);
	}

	private void replaceSwitchCase(Element el) {
		replaceWithTemplate(el, "*ngSwitchCase", "[ngSwitchCase]");
	}

	private void replaceSwitchDefault(Element el) {
		replaceWithTemplate(el, "*ngSwitchDefault", "ngSwitchDefault");
	}

	private void replaceNgFor(Element el) {
		String ngFor = el.attr("*ngFor");
		if (ngFor.isEmpty()) {
			return;
		}
		el.removeAttr("*ngFor");
		String[] itemAndListName = parseNgFor(ngFor);

		Element elClone = el.clone();

		new ArrayList<>(el.childNodes()).forEach(Node::remove);
		el.tagName(NG_TEMPLATE);
		el.attr("ngFor", true);
		el.attr(format("let-%s", itemAndListName[0]), true);
		el.attr("[ngForOf]", itemAndListName[1]);

		Map<ForOfVariable, String> vars = parseVariables(ngFor);
		for (Map.Entry<ForOfVariable, String> v : vars.entrySet()) {
			el.attr(format("let-%s", v.getValue()), v.getKey()
					.name());
		}

		el.appendChild(elClone);
	}

	private void replaceNgIf(Element el) {
		String ngIf = el.attr("*ngIf");
		if (ngIf.isEmpty()) {
			return;
		}

		el.removeAttr("*ngIf");

		Element elClone = el.clone();

		String[] splits = ngIf.split(";");
		for (int i = 0, n = splits.length; i < n; i++) {
			String split = splits[i].trim();
			if (i == 0) {
				el.attr("[ngIf]", split);
			} else if (split.startsWith("elseIf")) {
				List<String> elseIf = Stream.of(split.split("\\s"))
						.map(String::trim)
						.filter(s -> !s.isEmpty())
						.collect(toList());
				if (elseIf.size() != 3) {
					throw new ParseException("Malformed elseIf: %s", split);
				}
				el.attr(format("ngElseIf-%s", elseIf.get(2)), elseIf.get(1));
			} else if (split.startsWith("else")) {
				el.attr("ngElse", split.substring("else".length())
						.trim());
			}
		}

		el.tagName(NG_TEMPLATE);
		new ArrayList<>(el.childNodes()).forEach(Node::remove);
		el.appendChild(elClone);
	}

	@Override
	public void tail(Node node, int depth) {
		src.tail(node, depth);
	}

	static String[] parseNgFor(String expr) {
		Matcher matcher = FOR_OF_PATTERN.matcher(expr);
		if (!matcher.find()) {
			throw new ParseException("*ngFor expression malformed: %s", expr);
		}

		String itemName = matcher.group(1)
				.trim();
		String listName = matcher.group(2)
				.trim();

		int semi = listName.indexOf(';');
		if (semi > -1) {
			listName = listName.substring(0, semi);
		}

		return new String[] { itemName, listName };
	}

	static Map<ForOfVariable, String> parseVariables(String ngFor) {
		Map<ForOfVariable, String> result = new EnumMap<>(ForOfVariable.class);

		int semi = ngFor.indexOf(';');
		if (semi < 0) {
			return result;
		}

		String right = ngFor.substring(semi)
				.trim();

		String[] parts = right.split(";");
		for (String part : parts) {
			part = part.trim();
			if (part.isEmpty()) {
				continue;
			}
			Matcher matcher = VAR_DECL_PATTERN.matcher(part);
			if (!matcher.find()) {
				throw new NgoyException("Parse error in ngFor. Variables must be specified like: 'index as i; odd as o'");
			}

			String variable = matcher.group(1)
					.trim();
			ForOfVariable fv;
			try {
				fv = ForOfVariable.valueOf(variable);
			} catch (Exception e) {
				throw new NgoyException("Unknown ngFor variable: %s", variable);
			}
			String alias = matcher.group(2)
					.trim();
			result.put(fv, alias);
		}

		return result;
	}

	private void replaceWithTemplate(Element el, String microAttr, String replacedAttr) {
		if (!el.hasAttr(microAttr)) {
			return;
		}
		String value = el.attr(microAttr);
		el.removeAttr(microAttr);

		Element elClone = el.clone();

		el.attr(replacedAttr, value.isEmpty() ? null : value);

		el.tagName(NG_TEMPLATE);
		new ArrayList<>(el.childNodes()).forEach(Node::remove);
		el.appendChild(elClone);
	}
}
