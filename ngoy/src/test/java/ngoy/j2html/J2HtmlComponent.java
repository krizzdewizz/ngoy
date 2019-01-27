package ngoy.j2html;

import java.io.IOException;
import java.io.StringWriter;
import java.util.function.Consumer;

import j2html.tags.ContainerTag;
import j2html.tags.DomContent;
import j2html.tags.UnescapedText;
import ngoy.core.Inject;
import ngoy.core.Injector;
import ngoy.core.NgoyException;
import ngoy.core.OnDestroy;
import ngoy.core.OnInit;
import ngoy.core.OnRender;
import ngoy.core.Output;
import ngoy.core.internal.Ctx;
import ngoy.core.reflect.CmpReflectInfo;
import ngoy.core.reflect.CmpReflectInfoCache;
import ngoy.core.reflect.ReflectBinding;

/**
 * Base class for code-only components using j2html.
 * 
 * @author krizz
 */
public abstract class J2HtmlComponent implements OnRender {

	@Inject
	public Injector injector;

	protected <C> DomContent cmp(Class<C> clazz) {
		return cmp(clazz, null);
	}

	protected <C> ContainerTag cmp(Class<C> clazz, Consumer<C> init) {
		C cmp = injector.getNew(clazz);

		if (init != null) {
			init.accept(cmp);
		}

		if (cmp instanceof OnInit) {
			((OnInit) cmp).onInit();
		}

		DomContent content;

		if (cmp instanceof J2HtmlComponent) {
			content = ((J2HtmlComponent) cmp).content();
		} else {
			if (!(cmp instanceof OnRender)) {
				throw new NgoyException("Component must be an instance of %s or %s: %s", J2HtmlComponent.class.getName(), OnRender.class.getName(), cmp.getClass()
						.getName());
			}

			OnRender render = (OnRender) cmp;

			StringWriter sw = new StringWriter();
			Output out = new Ctx(cmp, injector, sw);
			render.onRender(out);
			render.onRenderEnd(out);

			content = new UnescapedText(sw.toString());
		}

		if (cmp instanceof OnDestroy) {
			((OnDestroy) cmp).onDestroy();
		}

		CmpReflectInfo info = CmpReflectInfoCache.INSTANCE.getInfo(clazz);
		ContainerTag hostEl = new ContainerTag(info.selector);

		writeHostBindings(cmp, info, hostEl);

		return hostEl.with(content);
	}

	private void writeHostBindings(Object cmp, CmpReflectInfo info, ContainerTag hostEl) {
		ReflectBinding.eval(cmp, info.classBindings, "class", null, hostEl::attr);
		ReflectBinding.eval(cmp, info.styleBindings, "style", null, hostEl::attr);
		ReflectBinding.eval(cmp, info.attrBindings, "", null, hostEl::attr);
	}

	@Override
	public void onRender(Output output) {
		try {
			content().render(output.getWriter());
		} catch (IOException e) {
			throw new RuntimeException("Error while rendering j2html DOM", e);
		}
	}

	abstract protected DomContent content();
}
