package org.ngoy.parser;

import static java.lang.String.format;

import org.ngoy.internal.parser.BufferedOutput;
import org.ngoy.util.Printer;

public class TextOutput extends BufferedOutput {

	private Printer printer;

	public TextOutput(Printer printer) {
		this.printer = printer;
	}

	protected void doPrint(String text, boolean isExpr) {
		String fmt = isExpr ? "ctx.printEscaped(%s);\n" : "ctx.print(\"%s\");\n";
		printer.print(format(fmt, text));
	}

	public void printEscaped(String text, boolean isExpr) {
		flush();
		String fmt = isExpr ? "ctx.printEscaped(%s);\n" : "ctx.printEscaped(\"%s\");\n";
		printer.print(format(fmt, text));
	}
}
