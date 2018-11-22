package ngoy.core.dom;

import jodd.jerry.Jerry;

public interface NodeVisitor {

	public class Default implements NodeVisitor {
		@Override
		public void head(Jerry node) {
		}

		@Override
		public void tail(Jerry node) {
		}
	}

	void head(Jerry node);

	void tail(Jerry node);
}