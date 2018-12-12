package ngoy.internal.parser.template;

import static java.lang.String.format;

import java.util.function.IntSupplier;

import ngoy.core.NgoyException;
import ngoy.internal.parser.BufferedOutput;

public class TextOutput extends BufferedOutput {

	private final Printer printer;
	private final IntSupplier depth;

	public TextOutput(Printer printer, IntSupplier depth) {
		this.printer = printer;
		this.depth = depth;
	}

	protected void doPrint(String text, boolean isExpr) {
		String fmt = isExpr ? "%sctx.printEscaped(%s);\n" : "%sctx.print(\"%s\");\n";
		printer.print(format(fmt, getDepth(), text));
	}

	public void printEscaped(String text, boolean isExpr) {
		if (isExpr) {
			flush();
			printer.print(format("%sctx.printEscaped(%s);\n", getDepth(), text));
		} else {
			throw new NgoyException("must noe be called");
		}
	}

	private String getDepth() {
		StringBuilder sb = new StringBuilder();
		for (int i = 0, n = depth.getAsInt(); i < n; i++) {
			sb.append("  ");
		}
		return sb.toString();
	}
}
