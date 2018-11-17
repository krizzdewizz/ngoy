package org.ngoy.internal.parser.visitor;

import jodd.jerry.Jerry;

public interface NodeVisitor {

	public class Default implements NodeVisitor {
		@Override
		public void head(Jerry node, int depth) {
		}

		@Override
		public void tail(Jerry node, int depth) {
		}
	}

	void head(Jerry node, int depth);

	void tail(Jerry node, int depth);

}
