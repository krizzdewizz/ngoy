package ngoy.hyperml;

import static java.util.stream.Collectors.joining;
import static ngoy.core.Util.escapeHtmlXml;

import java.io.IOException;
import java.io.Writer;
import java.util.LinkedList;

import ngoy.core.NgoyException;
import ngoy.core.Output;

/**
 * Base class for XML/HTML.
 * 
 * @author krizz
 * @param <T>
 */
public abstract class Base<T extends Base<?>> {
	public interface Handler {
		void startElementHead(String name);

		void endElementHead();

		void attribute(String name, String value);

		void endElement(String name);

		void text(String text, boolean escape);
	}

	public static class WriterHandler implements Handler {
		private final Writer out;

		public WriterHandler(Writer out) {
			this.out = out;
		}

		@Override
		public void startElementHead(String name) {
			write("<");
			write(name);
		}

		@Override
		public void endElementHead() {
			write(">");
		}

		@Override
		public void attribute(String name, String value) {
			write(" ");
			write(name);
			write("=\"");
			write(escapeHtmlXml(value));
			write("\"");
		}

		@Override
		public void endElement(String name) {
			write("</");
			write(name);
			write(">");
		}

		@Override
		public void text(String text, boolean escape) {
			write(escape ? escapeHtmlXml(text) : text);
		}

		private void write(String s) {
			try {
				out.write(s);
			} catch (IOException e) {
				throw NgoyException.wrap(e);
			}
		}
	}

	/**
	 * If given as last argument to {@link #$(String, Object...)}, will call
	 * {@link #$()} just after starting the element (auto-end). Resembles XML short
	 * closing of tags like <code>&lt;x/&gt;</code> --&gt; <code>$("x", $)</code>.
	 */
	public static final Object $ = new Object();

	private static String toString(Object obj) {
		return obj == null ? null : String.valueOf(obj);
	}

	protected abstract boolean isVoidElement(String name);

	protected abstract boolean escapeText();

	/**
	 * The underlying handler.
	 */
	private Handler handler;

	/**
	 * Stack of element names started so far. Using <code>LinkedList</code> instead
	 * of <code>Stack</code> because synch is not needed.
	 */
	protected final LinkedList<String> stack;

	public Base() {
		stack = new LinkedList<String>();
	}

	/**
	 * Builds the xml by transforming it to the given writer.
	 * <p>
	 * May be called several times.
	 * 
	 * @param out destination
	 */
	public void build(Writer out) {
		build(new WriterHandler(out));
	}

	/**
	 * Builds the xml by transforming it to the given output.
	 * <p>
	 * May be called several times.
	 * 
	 * @param out destination
	 */
	public void build(Output out) {
		build(out.getWriter());
	}

	public void build(Handler handler) {
		try {
			this.handler = handler;
			create();
			checkStack();
		} finally {
			handler = null;
		}
	}

	/**
	 * Maybe overridden by subclasses.
	 */
	protected void create() {
	}

	/**
	 * Checks that the name stack is empty upon endDocument().
	 * 
	 * @throws HyperXmlException if the name stack is not empty
	 */
	private void checkStack() {
		if (stack.isEmpty()) {
			return;
		}

		throw new NgoyException("Missing call to $(). Names left on stack: '%s'.", stack.stream()
				.collect(joining(", ")));
	}

	/**
	 * Outputs the given text using a <code>character()</code> call.
	 * 
	 * @param texts The text to output. The last item may be {@link #$}, in which
	 *              case the element is ended.
	 */
	public T text(Object... texts) {
		int nTexts = texts.length;
		boolean hasEnd = nTexts > 0 && texts[nTexts - 1] == $;
		boolean escapeText = escapeText();
		for (int i = 0, n = hasEnd ? nTexts - 1 : nTexts; i < n; i++) {
			handler.text(texts[i].toString(), escapeText);
		}
		return hasEnd ? $() : _this();
	}

	/**
	 * Starts an element, its attributes and an optional value. If the last argument
	 * is <code>$</code>, will call {@link #$(String, Object...)} w/o parameters,
	 * just after starting the element (auto-end).
	 * 
	 * @param name   The name of the element
	 * @param params attribute [name, value] pairs, optionally followed by a single
	 *               value.If the last argument is <code>$</code>, will call
	 *               {@link #$(String, Object...)} w/o parameters, just after
	 *               starting the element (auto-end). If the length of the array is
	 *               odd, the last element designates the value for the element. May
	 *               be empty. An attribute name may be namespace-prefixed.
	 */
	public T $(String name, Object... params) {
		handler.startElementHead(name);

		String elementValue = null;
		int nParams = params.length;
		boolean endElement = false;
		boolean voidElement = isVoidElement(name);

		if (nParams > 0) {

			if (params[nParams - 1] == $) {
				if (voidElement) {
					throw new NgoyException("void elements must not be ended: %s", name);
				}
				endElement = true;
				nParams--;
			}

			boolean paramsOdd = (nParams % 2) > 0;

			if (paramsOdd) {
				// last
				elementValue = toString(params[nParams - 1]);
			}

			if (nParams > 1) {
				if (paramsOdd) {
					nParams--;
				}
				for (int i = 0; i < nParams; i += 2) {
					String attrValue = toString(params[i + 1]);
					if (attrValue != null && !attrValue.isEmpty()) {
						String attrName = toString(params[i]);
						handler.attribute(attrName, attrValue);
					}
				}
			}
		}

		handler.endElementHead();

		if (!voidElement) {

			if (elementValue != null) {
				text(elementValue);
			}

			stack.add(name);
		}

		if (endElement) {
			$();
		}

		return _this();
	}

	/**
	 * Ends the last written element.
	 */
	public T $() {
		if (stack.isEmpty()) {
			throw new NgoyException("Too many calls to $().");
		}
		String name = stack.removeLast();
		handler.endElement(name);
		return _this();
	}

	/**
	 * The current handler.
	 * 
	 * @return handler, non-null during <code>build()</code> calls.
	 */
	public Handler getHandler() {
		return handler;
	}

	/**
	 * @param handler
	 */
	public void setHandler(Handler handler) {
		this.handler = handler;
	}

	@SuppressWarnings("unchecked")
	private T _this() {
		return (T) this;
	}
}
