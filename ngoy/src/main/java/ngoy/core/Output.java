package ngoy.core;

import java.io.Writer;

/**
 * The template's destination output.
 * 
 * @author krizz
 */
public interface Output {
	/**
	 * Writes the given string to the output.
	 * <p>
	 * Shorthand for <code>getWriter().write(string)</code>
	 * 
	 * @param string String to write
	 */
	void write(String string);

	Writer getWriter();
}
