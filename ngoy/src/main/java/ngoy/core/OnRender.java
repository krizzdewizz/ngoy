package ngoy.core;

/**
 * Render hook to write raw output.
 * <p>
 * A component or directive may implement this interface to write raw output.
 * <p>
 * Implementors have to care about proper escaping.
 *
 * @author krizz
 */
public interface OnRender {
    /**
     * Called before the component's content is rendered.
     *
     * @param output Output to write contents to
     */
    void onRender(Output output);

    /**
     * Called after the component's content is rendered.
     *
     * @param output Output to write contents to
     */
    default void onRenderEnd(Output output) {
        // noop
    }
}
