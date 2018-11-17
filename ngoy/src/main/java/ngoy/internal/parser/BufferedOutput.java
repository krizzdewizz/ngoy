package ngoy.internal.parser;

import static ngoy.core.Util.escape;

public abstract class BufferedOutput implements Output {
	private final StringBuilder buf = new StringBuilder();

	@Override
	public void print(String text, boolean isExpr, boolean escape,  String contentType) {
		if (isExpr) {
			flush();
			doPrint(text, true);
		} else {
			buf.append(escape ? escape(text, contentType) : text);
		}
	}

	protected abstract void doPrint(String text, boolean isExpr);

	@Override
	public void flush() {
		if (buf.length() > 0) {
			doPrint(buf.toString(), false);
			buf.setLength(0);
		}
	}
}
