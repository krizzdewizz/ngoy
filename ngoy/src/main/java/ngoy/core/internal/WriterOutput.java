package ngoy.core.internal;

import static ngoy.core.NgoyException.wrap;

import java.io.Writer;

public class WriterOutput implements Output {

	private final Writer out;

	public WriterOutput(Writer out) {
		this.out = out;
	}

	@Override
	public void write(String string) {
		try {
			out.write(string);
		} catch (Exception e) {
			throw wrap(e);
		}
	}
}
