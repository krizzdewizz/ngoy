package ngoy.hyperml;

import java.io.StringWriter;

import ngoy.core.Inject;
import ngoy.core.Injector;
import ngoy.core.OnCompileStyles;
import ngoy.core.OnRender;
import ngoy.core.Output;
import ngoy.hyperml.base.BaseMl;

/**
 * Base class for code-only components using {@link Html}.
 * 
 * @author krizz
 */
public abstract class HtmlComponent extends Html implements OnRender, OnCompileStyles {

	private Runnable renderer;

	@Inject
	public Injector injector;

	@Override
	protected Injector injector() {
		return injector;
	}

	@Override
	public void onRender(Output output) {
		try {
			renderer = this::content;
			build(output);
		} finally {
			renderer = null;
		}
	}

	@Override
	public String onCompileStyles() {
		StringWriter sw = new StringWriter();
		BaseMl<?> doc = stylesDocument();
		if (doc != null) {
			doc.build(sw);
		} else {
			try {
				renderer = this::styles;
				build(sw);
			} finally {
				renderer = null;
			}
		}
		return sw.toString();
	}

	protected BaseMl<?> stylesDocument() {
		return null;
	}

	protected void styles() {
	}

	abstract protected void content();

	@Override
	protected void create() {
		renderer.run();
	}
}
