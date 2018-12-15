package ngoy.internal.parser.template;

import static java.lang.String.format;

import java.util.function.IntSupplier;

import ngoy.internal.parser.BufferedOutput;

public class TextOutput extends BufferedOutput {

	public interface StringRef {
		String createRef(String text);
	}

	private final Printer printer;
	private final IntSupplier depth;
	private final StringRef stringRef;
	private final String printCall;

	public TextOutput(Printer printer, IntSupplier depth, StringRef stringRef, String contentType) {
		this.printer = printer;
		this.depth = depth;
		this.stringRef = stringRef;
		printCall = "text/plain".equals(contentType) ? "p" : "pe";
	}

	protected void doPrint(String text, boolean isExpr) {
		if (isExpr) {
			printEscaped(text);
		} else {
			String ref = stringRef.createRef(text);
			printer.print(format("%s%s.p(%s);\n", getDepth(), JavaTemplate.CTX_VAR, ref));
		}
	}

	public void printEscaped(String expr) {
		flush();
		printer.print(format("%s%s.%s(%s);\n", getDepth(), JavaTemplate.CTX_VAR, printCall, expr));
	}

	private String getDepth() {
		StringBuilder sb = new StringBuilder();
		for (int i = 0, n = depth.getAsInt(); i < n; i++) {
			sb.append("  ");
		}
		return sb.toString();
	}
}
