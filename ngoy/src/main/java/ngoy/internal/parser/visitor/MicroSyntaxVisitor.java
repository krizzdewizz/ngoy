package ngoy.internal.parser.visitor;

import static java.lang.String.format;
import static java.util.stream.Collectors.toList;
import static ngoy.core.dom.NgoyElement.setNodeName;
import static ngoy.core.dom.XDom.appendChild;
import static ngoy.core.dom.XDom.cloneNode;
import static ngoy.core.dom.XDom.removeContents;
import static ngoy.internal.parser.Parser.NG_TEMPLATE;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import jodd.jerry.Jerry;
import jodd.lagarto.dom.Element;
import ngoy.core.NgoyException;
import ngoy.core.dom.NodeVisitor;
import ngoy.internal.parser.ForOfVariable;
import ngoy.internal.parser.ParseException;

public class MicroSyntaxVisitor extends NodeVisitor.Default {

	private static final Pattern FOR_OF_PATTERN = Pattern.compile("let\\s*(.*)\\s*of\\s*(.*)");
	private static final Pattern VAR_DECL_PATTERN = Pattern.compile("(.*)\\s*as\\s*(.*)");

	private final NodeVisitor src;

	public MicroSyntaxVisitor(NodeVisitor src) {
		this.src = src;
	}

	@Override
	public void head(Jerry el) {
		if (el.get(0) instanceof Element) {
			replaceNgIf(el);
			replaceNgFor(el);
			replaceSwitchCase(el);
			replaceSwitchDefault(el);
		}

		src.head(el);
	}

	private void replaceSwitchCase(Jerry el) {
		replaceWithTemplate(el, "*ngSwitchCase", "[ngSwitchCase]");
	}

	private void replaceSwitchDefault(Jerry el) {
		replaceWithTemplate(el, "*ngSwitchDefault", "ngSwitchDefault");
	}

	private void replaceNgFor(Jerry el) {
		String ngFor = el.attr("*ngFor");
		if (ngFor == null) {
			return;
		}
		el.removeAttr("*ngFor");
		String[] itemAndListName = parseNgFor(ngFor);

		Jerry elClone = cloneNode(el);

		removeContents(el);
		setNodeName(el, NG_TEMPLATE);
		el.attr("ngFor", null);
		el.attr(format("let-%s", itemAndListName[0]), null);
		el.attr("[ngForOf]", itemAndListName[1]);

		Map<ForOfVariable, String> vars = parseVariables(ngFor);
		for (Map.Entry<ForOfVariable, String> v : vars.entrySet()) {
			el.attr(format("let-%s", v.getValue()), v.getKey()
					.name());
		}

		appendChild(el, elClone);
	}

	private void replaceNgIf(Jerry el) {
		String ngIf = el.attr("*ngIf");
		if (ngIf == null) {
			return;
		}

		el.removeAttr("*ngIf");

		Jerry elClone = cloneNode(el);

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

		setNodeName(el, NG_TEMPLATE);
		removeContents(el);
		appendChild(el, elClone);
	}

	@Override
	public void tail(Jerry node) {
		src.tail(node);
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

	private void replaceWithTemplate(Jerry el, String microAttr, String replacedAttr) {
		if (!el.get(0)
				.hasAttribute(microAttr)) {
			return;
		}
		String value = el.attr(microAttr);

		el.removeAttr(microAttr);
		Jerry elClone = cloneNode(el);

		el.attr(replacedAttr, value);

		setNodeName(el, NG_TEMPLATE);
		removeContents(el);
		appendChild(el, elClone);
	}
}
