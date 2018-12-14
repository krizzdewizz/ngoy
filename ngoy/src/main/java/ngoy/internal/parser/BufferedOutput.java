package ngoy.internal.parser;

import static ngoy.core.Util.escape;

import ngoy.internal.parser.template.JavaTemplate;

public abstract class BufferedOutput {
	private final StringBuilder buf = new StringBuilder();

	public void print(String text, boolean isExpr, boolean escape, String contentType) {
		if (isExpr) {
			flush();
			doPrint(text, true);
		} else {
			buf.append(escape ? JavaTemplate.escapeJava(escape(text, contentType)) : text);
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
