package ngoy.hyperml;

import ngoy.core.OnCompileStyles;
import ngoy.core.OnRender;
import ngoy.hyperml.base.HtmlBaseComponent;

/**
 * Base class for code-only components using {@link Html}.
 * 
 * @author krizz
 */
public abstract class HtmlComponent extends HtmlBaseComponent<Html> implements OnRender, OnCompileStyles {
}
