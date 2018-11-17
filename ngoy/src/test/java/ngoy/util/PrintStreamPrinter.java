package ngoy.util;

import java.io.PrintStream;

public class PrintStreamPrinter implements Printer {
	private final PrintStream printer;

	public PrintStreamPrinter(PrintStream printer) {
		this.printer = printer;
	}

	@Override
	public void print(String text) {
		printer.print(text);
	}
}