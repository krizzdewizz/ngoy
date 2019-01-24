package ngoy.common;

import ngoy.core.OnRender;
import ngoy.core.Output;
import ngoy.hyperml.Html;

/**
 * Base class for template-less components using {@link Html}.
 * 
 * @author christian.oetterli
 *
 */
public abstract class AHtmlComponent extends Html implements OnRender {
	@Override
	public void ngOnRender(Output output) {
		build(output);
	}

	@Override
	protected abstract void create();
}
