package ngoy.internal.parser;

import static ngoy.core.Util.escapeHtmlXml;
import static ngoy.core.Util.escapeJava;

public abstract class BufferedOutput {
	private final StringBuilder buf = new StringBuilder();
	private final boolean escapeText;

	public BufferedOutput(String contentType) {
		escapeText = !"text/plain".equals(contentType);
	}

	public void print(String text, boolean isExpr, boolean escape) {
		if (isExpr) {
			flush();
			doPrint(text, true);
		} else {
			buf.append(escape ? escapeJava(escapeText ? escapeHtmlXml(text) : text) : text);
		}
	}

	protected abstract void doPrint(String text, boolean isExpr);

	public void flush() {
		if (buf.length() > 0) {
			doPrint(buf.toString(), false);
			buf.setLength(0);
		}
	}
}
