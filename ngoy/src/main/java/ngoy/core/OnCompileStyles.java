package ngoy.core;

/**
 * Compile css styles lifecycle hook.
 * <p>
 * Implementors can add css styles to the html document upon compilation.
 *
 * @author krizz
 */
public interface OnCompileStyles {
    /**
     * Called upon compilation of a html document - not called at 'runtime'.
     *
     * @return css style declarations to add to the document
     */
    String onCompileStyles();
}
