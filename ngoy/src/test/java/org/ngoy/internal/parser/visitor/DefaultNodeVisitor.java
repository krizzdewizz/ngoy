package org.ngoy.internal.parser.visitor;

import org.jsoup.nodes.Node;
import org.jsoup.select.NodeVisitor;

public class DefaultNodeVisitor implements NodeVisitor {
	@Override
	public void tail(Node node, int depth) {
	}

	@Override
	public void head(Node node, int depth) {
	}
}