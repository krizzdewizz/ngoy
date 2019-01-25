package ngoy.common;

import java.io.StringWriter;

import ngoy.core.OnCompileStyles;
import ngoy.core.OnRender;
import ngoy.core.Output;
import ngoy.hyperml.Html;

/**
 * Base class for template-less components using {@link Html}.
 * 
 * @author christian.oetterli
 *
 */
public abstract class AHtmlComponent extends Html implements OnRender, OnCompileStyles {

	private static enum Mode {
		TEMPLATE, STYLES
	}

	private Mode mode;

	@Override
	public void onRender(Output output) {
		try {
			mode = Mode.TEMPLATE;
			build(output);
		} finally {
			mode = null;
		}
	}

	@Override
	public String onCompileStyles() {
		try {
			mode = Mode.STYLES;
			StringWriter sw = new StringWriter();
			build(sw);
			return sw.toString();
		} finally {
			mode = null;
		}
	}

	protected void styles() {
	}

	abstract protected void template();

	@Override
	protected void create() {
		switch (mode) {
		case TEMPLATE:
			template();
			break;
		case STYLES:
			styles();
			break;
		}
	}
}
