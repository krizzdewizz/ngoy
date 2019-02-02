package ngoy.internal.parser.template;

import static ngoy.core.Util.sourceClassName;

import hyperml.base.Util;

public class CodeBuilder {
	private int depth;
	private final Printer printer;

	public CodeBuilder() {
		this.printer = new Printer();
	}

	public CodeBuilder create() {
		doCreate();
		return this;
	}

	protected void doCreate() {
		// no action
	}

	public CodeBuilder $$(Object... all) {
		for (Object s : all) {
			doPrint(s instanceof Class ? sourceClassName(((Class<?>) s)) : s);
		}
		return this;
	}

	protected void doPrint(Object object) {
		getPrinter().print(String.valueOf(object));
	}

	public CodeBuilder $(Object... stringss) {
		Object[] strings = Util.flatten(stringss);

		String last = strings.length > 0 ? strings[strings.length - 1].toString()
				.trim() : "";
		if (last.endsWith("}") || last.startsWith("}")) {
			depth--;
		}

		addIndent();

		$$(strings);

		doPrint("\n");

		if (last.endsWith("{")) {
			depth++;
		}

		return this;
	}

	protected void addIndent() {
		for (int i = 0; i < depth; i++) {
			doPrint("  ");
		}
	}

	@Override
	public String toString() {
		return getPrinter().toString();
	}

	protected int getDepth() {
		return depth;
	}

	protected Printer getPrinter() {
		return printer;
	}
}