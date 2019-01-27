package ngoy.hyperml;

import java.io.StringWriter;

import ngoy.core.Inject;
import ngoy.core.Injector;
import ngoy.core.OnCompileStyles;
import ngoy.core.OnRender;
import ngoy.core.Output;

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
		try {
			renderer = this::styles;
			StringWriter sw = new StringWriter();
			build(sw);
			return sw.toString();
		} finally {
			renderer = null;
		}
	}

	protected void styles() {
	}

	abstract protected void content();

	@Override
	protected void create() {
		renderer.run();
	}
}
