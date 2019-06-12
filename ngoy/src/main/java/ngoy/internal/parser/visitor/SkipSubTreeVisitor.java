package ngoy.internal.parser.visitor;

import jodd.jerry.Jerry;
import jodd.lagarto.dom.Document;
import ngoy.core.dom.NodeVisitor;

import static ngoy.core.dom.XDom.isEqualNode;

public class SkipSubTreeVisitor implements NodeVisitor {

    private final NodeVisitor target;
    private boolean skip;
    private Jerry skipNode;

    public SkipSubTreeVisitor(NodeVisitor target) {
        this.target = target;
    }

    public void skipSubTree(Jerry node) {
        this.skipNode = node;
    }

    private boolean skip(Jerry node) {
        return skip || node.get(0) instanceof Document;
    }

    public boolean shallSkip(Jerry node) {
        return skipNode != null && isEqualNode(skipNode, node.parent());
    }

    @Override
    public void start(Jerry node) {
        if (skip(node)) {
            return;
        }

        if (shallSkip(node)) {
            skip = true;
            return;
        }

        target.start(node);
    }

    @Override
    public void end(Jerry node) {
        if (shallSkip(node)) {
            skip = false;
            return;
        }

        if (skip(node)) {
            return;
        }

        target.end(node);
    }
}
