package ngoy.core;

import static ngoy.core.NgoyException.wrap;

import java.io.Writer;

/**
 * The template's destination output.
 * 
 * @author krizz
 */
public class Output {
	private final Writer writer;

	public Output(Writer writer) {
		this.writer = writer;
	}

	/**
	 * Writes the given string to the output.
	 * <p>
	 * Shorthand for <code>getWriter().write(string)</code>
	 * 
	 * @param string String to write
	 */
	public void write(String string) {
		try {
			writer.write(string);
		} catch (Exception e) {
			throw wrap(e);
		}
	}

	public Writer getWriter() {
		return writer;
	}
}
