package ngoy.hyperml.base;

import java.io.OutputStream;
import java.io.Writer;
import java.util.ArrayList;
import java.util.function.Consumer;

import hyperml.HyperMlException;
import hyperml.base.HtmlBase;
import ngoy.core.Injector;
import ngoy.core.OnDestroy;
import ngoy.core.OnInit;
import ngoy.core.OnRender;
import ngoy.core.Output;
import ngoy.core.internal.Ctx;
import ngoy.core.internal.TemplateCompiler;
import ngoy.core.internal.TemplateRender;
import ngoy.core.internal.TemplateRenderCache;
import ngoy.core.reflect.CmpReflectInfo;
import ngoy.core.reflect.CmpReflectInfoCache;
import ngoy.core.reflect.ReflectBinding;
import ngoy.core.reflect.ReflectInput;

/**
 * HTML Elements and attributes, css and components.
 * 
 * @author krizz
 */
public class NgoyHtmlBase<T extends NgoyHtmlBase<?>> extends HtmlBase<NgoyHtmlBase<T>> {

	@SuppressWarnings("serial")
	private static class ParamsWithInit extends ArrayList<Object> {
		Consumer<?> init;
		Object classList;
		Object styleList;
		CmpInstance cmpInstance;

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
		final Object cmp;
		final OnRender render;
		final CmpReflectInfo info;

		CmpInstance(Object cmp, OnRender render, CmpReflectInfo info) {
			this.cmp = cmp;
			this.render = render;
			this.info = info;
		}
	}

	private final ParamsHandler<ParamsWithInit> paramsHandler = new ParamsHandler<ParamsWithInit>() {

		@Override
		public ParamInfo<ParamsWithInit> init(Object nameOrClass, Object... params) {
			ParamsWithInit paramsWithInit = new ParamsWithInit(params);
			CmpInstance cmp = handleCmp(nameOrClass, paramsWithInit);
			paramsWithInit.cmpInstance = cmp;
			String name = cmp != null ? cmp.info.selector : nameOrClass.toString();
			return new ParamInfo<ParamsWithInit>(name, paramsWithInit.toArray(), paramsWithInit, getParamsHandler());
		}

		@Override
		public boolean applyAttribute(ParamsWithInit obj, String name, Object value) {
			if (obj.cmpInstance == null) {
				return false;
			}
			return applyInput(obj.cmpInstance, name, value);
		}

		@Override
		public void endElementHead(ParamsWithInit obj) {
			writeHostBindings(obj);
		}

		@Override
		public void start(ParamsWithInit obj) {
			if (obj.cmpInstance != null) {
				componentStart(obj.cmpInstance);
			}
		}

		@Override
		public void end(ParamsWithInit obj) {
			if (obj.cmpInstance != null) {
				componentEnd(obj.cmpInstance);
			}
		}
	};

	/**
	 * Set while ngoy is rendering.
	 */
	private Output out;

	public NgoyHtmlBase() {
	}

	public NgoyHtmlBase(Writer writer) {
		super(writer);
	}

	public NgoyHtmlBase(OutputStream out) {
		super(out);
	}

	/**
	 * Must be overriden to return non-null when components are used.
	 */
	protected Injector injector() {
		return null;
	}

	private void writeHostBindings(ParamsWithInit params) {
		CmpInstance cmp = params.cmpInstance;
		Object cmpp = cmp == null ? null : cmp.cmp;
		ReflectBinding.eval(cmpp, cmp == null ? null : cmp.info.classBindings, "class", params.classList, this::_attribute);
		ReflectBinding.eval(cmpp, cmp == null ? null : cmp.info.styleBindings, "style", params.styleList, this::_attribute);
		if (cmp != null) {
			ReflectBinding.eval(cmpp, cmp.info.attrBindings, "", null, this::_attribute);
		}
	}

	private void componentStart(CmpInstance cmp) {
		cmp.render.onRender(out);
	}

	protected void componentEnd(CmpInstance cmp) {
		cmp.render.onRenderEnd(out);

		if (cmp instanceof OnDestroy) {
			((OnDestroy) cmp).onDestroy();
		}
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
		_$(nameOrClass, list(init, params));
		return _this();
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
		_$(nameOrClass, list(init, params));
		return _this();
	}

	@SuppressWarnings("unchecked")
	@Override
	protected ParamsHandler<ParamsWithInit> getParamsHandler() {
		return paramsHandler;
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
			throw new HyperMlException(e, "Error while setting component input %s.%s: %s", cmp.cmp.getClass()
					.getName(), attrName, e.getMessage());
		}
	}

	private CmpInstance handleCmp(Object nameOrClass, ParamsWithInit pInit) {
		Injector injector = injector();
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

		OnRender render;

		if (cmp instanceof OnRender) {
			render = (OnRender) cmp;
		} else {
			TemplateRender templateRender = TemplateRenderCache.INSTANCE.compile(cmpClass, injector.get(TemplateCompiler.class));

			Object cmpp = cmp;
			render = out -> templateRender.render(new Ctx(cmpp, injector, out.getWriter()));
		}

		if (pInit.init != null) {
			@SuppressWarnings({ "rawtypes", "unchecked" })
			Consumer<Object> initt = (Consumer) pInit.init;
			initt.accept(cmp);
		}

		if (cmp instanceof OnInit) {
			((OnInit) cmp).onInit();
		}

		return new CmpInstance(cmp, render, cmpInfo);
	}

	@SuppressWarnings("unchecked")
	private T _this() {
		return (T) this;
	}

}
