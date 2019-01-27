package ngoy.core;

/**
 * Minifies the component's styles/styleUrls css.
 * <p>
 * You may want to provide another. The default does nothing.
 * 
 * @author krizz
 */
public interface MinifyCss {
	/**
	 * Minifies css.
	 * 
	 * @param css Css to minify
	 * @return Minified css
	 */
	String minifyCss(String css);
}
