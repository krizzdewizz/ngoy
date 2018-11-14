package org.ngoy.internal.parser.visitor;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Node;
import org.jsoup.select.NodeVisitor;

public class SkipSubTreeVisitor implements NodeVisitor {

	private final NodeVisitor target;
	private boolean skip;
	private Node skipNode;

	public SkipSubTreeVisitor(NodeVisitor target) {
		this.target = target;
	}

	public void skipSubTree(Node node) {
		this.skipNode = node;
	}

	private boolean skip(Node node) {
		return node instanceof Document || skip;
	}

	public boolean shallSkip(Node node) {
		return skipNode != null && skipNode.equals(node.parent());
	}

	@Override
	public void head(Node node, int depth) {
		if (skip(node)) {
			return;
		}

		if (shallSkip(node)) {
			skip = true;
			return;
		}

		target.head(node, depth);
	}

	@Override
	public void tail(Node node, int depth) {
		if (shallSkip(node)) {
			skip = false;
			return;
		}

		if (skip(node)) {
			return;
		}

		target.tail(node, depth);
	}
}
