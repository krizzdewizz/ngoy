package ngoy.internal.parser.template;

import java.io.StringWriter;

public class Printer {
	private final StringWriter writer = new StringWriter();

	public void print(String text) {
		writer.write(text);
	}

	@Override
	public String toString() {
		return writer.toString();
	}
}