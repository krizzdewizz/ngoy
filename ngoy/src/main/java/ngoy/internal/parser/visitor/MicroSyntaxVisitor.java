package ngoy.internal.parser.visitor;

import static java.lang.String.format;
import static java.util.stream.Collectors.toList;
import static jodd.lagarto.dom.Node.NodeType.ELEMENT;
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
import ngoy.core.NgoyException;
import ngoy.core.dom.NodeVisitor;
import ngoy.internal.parser.ForOfDef;
import ngoy.internal.parser.ForOfVariable;
import ngoy.internal.parser.ParseException;

public class MicroSyntaxVisitor extends NodeVisitor.Default {

	private static final Pattern VAR_DECL_PATTERN = Pattern.compile("(.*)\\s*as\\s*(.*)");

	private final NodeVisitor target;

	public MicroSyntaxVisitor(NodeVisitor target) {
		this.target = target;
	}

	@Override
	public void start(Jerry el) {
		if (el.get(0)
				.getNodeType() == ELEMENT) {
			replaceNgIf(el);
			replaceNgFor(el);

			if (el.attr("[ngSwitch]") != null) {
				el.children()
						.each((child, index) -> {
							replaceSwitchCase(child);
							replaceSwitchDefault(child);
							return true;
						});
			}
		}

		target.start(el);
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
		ForOfDef forOfDef = parseNgFor(ngFor);

		Jerry elClone = cloneNode(el);

		removeContents(el);
		setNodeName(el, NG_TEMPLATE);
		el.attr("ngFor", null);
		el.attr(format("let-%s", forOfDef.itemName), null);
		el.attr(format("let-item-type"), forOfDef.itemType);
		el.attr("[ngForOf]", forOfDef.listName);

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
	public void end(Jerry node) {
		target.end(node);
	}

	static ForOfDef parseNgFor(String expr) {
		int delimLen = 1;
		int pos = expr.indexOf(":");
		if (pos < 0) {
			delimLen = 2;
			pos = expr.indexOf("of");
		}

		if (pos < 0) {
			throw new ParseException("*ngFor expression malformed: %s", expr);
		}

		String left = expr.substring(0, pos);
		String[] leftSplits = left.split("\\s");
		String itemName = leftSplits[1].trim();

		String right = expr.substring(pos + delimLen);

		int semiPos = right.indexOf(';');
		if (semiPos >= 0) {
			right = right.substring(0, semiPos);
		}

		String listName = right.trim();
		String itemType = leftSplits[0].trim();
		return new ForOfDef(itemType, itemName, listName);
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
