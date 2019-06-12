package ngoy.core.dom;

import jodd.jerry.Jerry;

/**
 * Callback for {@link XDom#accept(Jerry, NodeVisitor)}.
 *
 * @author krizz
 */
public interface NodeVisitor {

    /**
     * Implementation that does nothing.
     *
     * @author krizz
     */
    class Default implements NodeVisitor {
        @Override
        public void start(Jerry node) {
        }

        @Override
        public void end(Jerry node) {
        }
    }

    /**
     * Called when the node is being traversed.
     *
     * @param node
     */
    void start(Jerry node);

    /**
     * Called after all the node's descendants have been traversed.
     *
     * @param node
     */
    void end(Jerry node);
}