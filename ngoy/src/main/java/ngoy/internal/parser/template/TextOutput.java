package ngoy.internal.parser.template;

import static java.lang.String.format;

import java.util.function.IntSupplier;

import ngoy.internal.parser.BufferedOutput;

public class TextOutput extends BufferedOutput {

	public interface ByteArrayRef {
		String createRef(String text);
	}

	private final Printer printer;
	private final IntSupplier depth;
	private final ByteArrayRef byteArrayRef;

	public TextOutput(Printer printer, IntSupplier depth, ByteArrayRef byteArrayRef) {
		this.printer = printer;
		this.depth = depth;
		this.byteArrayRef = byteArrayRef;
	}

	protected void doPrint(String text, boolean isExpr) {
		if (isExpr) {
			printEscaped(text);
		} else {
			String ref = byteArrayRef.createRef(text);
			printer.print(format("%s%s.pb(%s);\n", getDepth(), JavaTemplate.CTX_VAR, ref));
		}
	}

	public void printEscaped(String expr) {
		flush();
		printer.print(format("%s%s.pe(%s);\n", getDepth(), JavaTemplate.CTX_VAR, expr));
	}

	private String getDepth() {
		StringBuilder sb = new StringBuilder();
		for (int i = 0, n = depth.getAsInt(); i < n; i++) {
			sb.append("  ");
		}
		return sb.toString();
	}
}
