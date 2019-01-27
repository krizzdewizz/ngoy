package ngoy.core;

/**
 * Transforms the component's styles/styleUrls css before it is written to the
 * document.
 * <p>
 * May be used to minify css or transform scss to css...
 * <p>
 * You may want to provide another. The default does nothing.
 * 
 * @author krizz
 */
public interface CssTransform {
	/**
	 * Transforms css.
	 * 
	 * @param css Css to transform
	 * @return Transformed css
	 */
	String transformCss(String css);
}
