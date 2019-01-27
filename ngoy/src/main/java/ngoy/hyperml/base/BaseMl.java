package ngoy.hyperml.base;

import static java.util.Arrays.asList;
import static java.util.stream.Collectors.joining;
import static ngoy.core.NgoyException.wrap;
import static ngoy.core.Util.escapeHtmlXml;

import java.io.Writer;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;

import ngoy.core.Injector;
import ngoy.core.NgoyException;
import ngoy.core.OnDestroy;
import ngoy.core.OnInit;
import ngoy.core.OnRender;
import ngoy.core.Output;
import ngoy.core.reflect.CmpReflectInfo;
import ngoy.core.reflect.CmpReflectInfoCache;
import ngoy.core.reflect.ReflectBinding;
import ngoy.core.reflect.ReflectInput;

/**
 * Base class for XML/HTML.
 * 
 * @author krizz
 * @param <T>
 */
public abstract class BaseMl<T extends BaseMl<?>> {

	@SuppressWarnings("serial")
	private static class ParamsWithInit extends ArrayList<Object> {
		Consumer<?> init;
		Object classList;
		Object styleList;

		ParamsWithInit(Object... all) {
			for (int i = 0, n = all.length; i < n; i++) {
				Object it = all[i];
				boolean isName = i == 0;
				if (isName && "class".equals(it)) {
					classList = all[i + 1];
					i++;
					continue;
				}
				if (isName && "style".equals(it)) {
					styleList = all[i + 1];
					i++;
					continue;
				}
				if (it instanceof Consumer<?>) {
					init = (Consumer<?>) it;
				} else {
					add(it);
				}
			}
		}
	}

	private static class CmpInstance {
		final OnRender cmp;
		final CmpReflectInfo info;

		CmpInstance(OnRender cmp, CmpReflectInfo info) {
			this.cmp = cmp;
			this.info = info;
		}
	}

	/**
	 * If given as last argument to {@link #$(Object, Object...)}, will call
	 * {@link #$()} just after starting the element (auto-end). Resembles XML short
	 * closing of tags like <code>&lt;x/&gt;</code> --&gt; <code>$("x", $)</code>.
	 */
	public static final Object $ = new Object();

	private static String toString(Object obj) {
		return obj == null ? null : obj.toString();
	}

	/**
	 * Stack of element names started so far. Using <code>LinkedList</code> instead
	 * of <code>Stack</code> because synch is not needed.
	 */
	protected final LinkedList<Object> stack = new LinkedList<>();

	/**
	 * Set while ngoy is rendering.
	 */
	private Output out;

	public Writer writer;

	protected abstract boolean isVoidElement(String name);

	protected abstract boolean escapeText();

	/**
	 * Must be overriden to return non-null when components are used.
	 */
	protected Injector getInjector() {
		return null;
	}

	/**
	 * Builds the xml by transforming it to the given writer.
	 * <p>
	 * May be called several times.
	 * 
	 * @param out destination
	 */
	public void build(Writer out) {
		try {
			writer = out;
			create();
			checkStack();
		} finally {
			writer = null;
		}
	}

	/**
	 * Builds the xml by transforming it to the given output.
	 * <p>
	 * May be called several times.
	 * 
	 * @param out destination
	 */
	public void build(Output out) {
		try {
			this.out = out;
			build(out.getWriter());
		} finally {
			this.out = null;
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
				.map(Object::toString)
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
			Object text = texts[i];
			if (text != null && !text.toString()
					.isEmpty()) {
				_text(text.toString(), escapeText);
			}
		}
		return hasEnd ? $() : _this();
	}

	private Object[] merge(Consumer<?> init, Object... params) {
		List<Object> ps = new ArrayList<>();
		ps.add(init);
		ps.addAll(asList(params));
		return ps.toArray();
	}

	/**
	 * Renders a component instance, given an initializer function.
	 * <p>
	 * Example:
	 * 
	 * <pre>
	 * this.&lt;PersonCmp&gt;$(&quot;person&quot;, cmp -&gt; cmp.name = &quot;peter&quot;, $);
	 * </pre>
	 * 
	 * @param nameOrClass Maybe a component selector or a component {@link Class} -
	 *                    must be an instance of {@link OnRender}
	 * @param init        Used to initialize the component instance - probably it's
	 *                    inputs
	 * @return this
	 */
	public <C> T $(Object nameOrClass, Consumer<C> init, Object... params) {
		return _$(nameOrClass, merge(init, params));
	}

	/**
	 * Renders a component instance, given an initializer function.
	 * <p>
	 * Example:
	 * 
	 * <pre>
	 * $(PersonCmp.class, cmp -&gt; cmp.name = &quot;peter&quot;, $);
	 * </pre>
	 * 
	 * @param nameOrClass Component class - must be an instance of {@link OnRender}
	 * @param init        Used to initialize the component instance - probably it's
	 *                    inputs
	 * @return this
	 */
	public <C> T $(Class<C> nameOrClass, Consumer<C> init, Object... params) {
		return _$(nameOrClass, merge(init, params));
	}

	/**
	 * Starts an element, its attributes and an optional value. If the last argument
	 * is <code>$</code>, will call {@link #$(Object, Object...)} w/o parameters,
	 * just after starting the element (auto-end).
	 * 
	 * @param nameOrClass The name of the element or a component {@link Class}
	 * @param params      attribute [name, value] pairs, optionally followed by a
	 *                    single value.If the last argument is <code>$</code>, will
	 *                    call {@link #$(Object, Object...)} w/o parameters, just
	 *                    after starting the element (auto-end). If the length of
	 *                    the array is odd, the last element designates the value
	 *                    for the element. May be empty. An attribute name may be
	 *                    namespace-prefixed.
	 */
	public T $(Object nameOrClass, Object... params) {
		return _$(nameOrClass, params);
	}

	private T _$(Object nameOrClass, Object... params) {

		ParamsWithInit paramsWithInit = new ParamsWithInit(params);
		params = paramsWithInit.toArray();

		int nParams = params.length;
		boolean endElement = nParams > 0 && params[nParams - 1] == $;

		CmpInstance cmp = handleCmp(nameOrClass, paramsWithInit.init);

		String name = cmp != null ? cmp.info.selector : nameOrClass.toString();

		_startElementHead(name);

		String elementValue = null;

		boolean voidElement = isVoidElement(name);

		if (nParams > 0) {
			if (endElement) {
				if (voidElement) {
					throw new NgoyException("void elements must not be ended: %s", name);
				}
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
					Object attrValue = params[i + 1];
					if (attrValue != null && !attrValue.toString()
							.isEmpty()) {
						String attrName = attrName(params[i]);
						if (cmp == null || !applyInput(cmp, attrName, attrValue)) {
							_attribute(attrName, attrValue.toString());
						}
					}
				}
			}
		}

		writeHostBindings(cmp, paramsWithInit);

		_endElementHead();

		if (cmp != null) {
			componentStart(cmp.cmp);
		}

		if (!voidElement) {

			if (elementValue != null) {
				text(elementValue);
			}

			stack.add(name);

			if (cmp != null) {
				stack.add(cmp.cmp);
			}
		}

		if (endElement) {
			$();
		}

		return _this();
	}

	private void writeHostBindings(CmpInstance cmp, ParamsWithInit params) {
		Object cmpp = cmp == null ? null : cmp.cmp;
		ReflectBinding.eval(cmpp, cmp == null ? null : cmp.info.classBindings, "class", params.classList, this::_attribute);
		ReflectBinding.eval(cmpp, cmp == null ? null : cmp.info.styleBindings, "style", params.styleList, this::_attribute);
		if (cmp != null) {
			ReflectBinding.eval(cmp.cmp, cmp.info.attrBindings, "", null, this::_attribute);
		}
	}

	private String attrName(Object name) {
		if (name == null) {
			throw new NgoyException("attribute name must not be null");
		} else if (name.toString()
				.isEmpty()) {
			throw new NgoyException("attribute name must not be empty");
		}

		return name.toString();
	}

	/**
	 * @return true if the input was applied, false if attrName is not a known input
	 *         of cmp
	 */
	private boolean applyInput(CmpInstance cmp, String attrName, Object attrValue) {
		ReflectInput input = cmp.info.inputs.get(attrName);
		if (input == null) {
			return false;
		}

		try {
			input.apply(cmp.cmp, attrValue);
			return true;
		} catch (Throwable e) {
			throw new NgoyException(e, "Error while setting component input %s.%s: %s", cmp.cmp.getClass()
					.getName(), attrName, e.getMessage());
		}
	}

	private CmpInstance handleCmp(Object nameOrClass, Consumer<?> init) {
		Injector injector = getInjector();
		if (injector == null) {
			return null;
		}

		Object cmp = null;
		String selector;
		Class<?> cmpClass = null;
		CmpReflectInfo cmpInfo = null;
		if (nameOrClass instanceof Class<?>) {
			cmpClass = (Class<?>) nameOrClass;
			cmpInfo = CmpReflectInfoCache.INSTANCE.getInfo(cmpClass);
			cmp = injector.getNew(cmpClass);
			selector = cmpInfo.selector;
		} else {
			selector = nameOrClass.toString();
			cmp = injector.getNewCmp(selector);
			if (cmp != null) {
				cmpClass = cmp.getClass();
				cmpInfo = CmpReflectInfoCache.INSTANCE.getInfo(cmpClass);
			}
		}

		if (cmp == null) {
			return null;
		}

		if (!(cmp instanceof OnRender)) {
			throw new NgoyException("Component must be an instance of %s: %s", OnRender.class.getName(), cmp.getClass()
					.getName());
		}

		if (init != null) {
			@SuppressWarnings({ "rawtypes", "unchecked" })
			Consumer<Object> initt = (Consumer) init;
			initt.accept(cmp);
		}

		if (cmp instanceof OnInit) {
			((OnInit) cmp).onInit();
		}

		return new CmpInstance((OnRender) cmp, cmpInfo);
	}

	/**
	 * Ends the last written element.
	 */
	public T $() {
		if (stack.isEmpty()) {
			throw new NgoyException("Too many calls to $().");
		}
		Object name = stack.removeLast();
		if (name instanceof OnRender) {
			componentEnd((OnRender) name);
			$(); // end host element
		} else {
			_endElement(name.toString());
		}
		return _this();
	}

	private void componentStart(OnRender cmp) {
		cmp.onRender(out);
	}

	protected void componentEnd(OnRender cmp) {
		cmp.onRenderEnd(out);

		if (cmp instanceof OnDestroy) {
			((OnDestroy) cmp).onDestroy();
		}
	}

	@SuppressWarnings("unchecked")
	private T _this() {
		return (T) this;
	}

	private void _startElementHead(String name) {
		_write("<");
		_write(name);
	}

	private void _endElementHead() {
		_write(">");
	}

	private void _attribute(String name, String value) {
		_write(" ");
		_write(name);
		_write("=\"");
		_write(escapeHtmlXml(value));
		_write("\"");
	}

	private void _endElement(String name) {
		_write("</");
		_write(name);
		_write(">");
	}

	private void _text(String text, boolean escape) {
		_write(escape ? escapeHtmlXml(text) : text);
	}

	private void _write(String s) {
		try {
			writer.write(s);
		} catch (Exception e) {
			throw wrap(e);
		}
	}
}
