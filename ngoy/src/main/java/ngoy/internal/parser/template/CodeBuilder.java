package ngoy.internal.parser.template;

public class CodeBuilder {
	protected int depth;
	protected final Printer printer;

	public CodeBuilder(Printer printer) {
		this.printer = printer;
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
			doPrint(s instanceof Class ? ((Class<?>) s).getName() : s);
		}
		return this;
	}

	protected void doPrint(Object object) {
		printer.print(String.valueOf(object));
	}

	public CodeBuilder $(Object... strings) {

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
		return printer.toString();
	}
}