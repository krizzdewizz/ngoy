package ngoy.core.internal;

import java.util.Iterator;

@SuppressWarnings("rawtypes")
public class IterableWithVariables implements Iterable {

	public static class Iter implements Iterator {

		private final Iterator targetIter;

		public int index;
		public boolean first;
		public boolean last;
		public boolean even;
		public boolean odd;

		public Iter(Iterator targetIter) {
			this.targetIter = targetIter;
			index = -1;
		}

		@Override
		public boolean hasNext() {
			return targetIter.hasNext();
		}

		@Override
		public Object next() {
			Object obj = targetIter.next();

			index++;

			first = index == 0;
			last = !hasNext();
			even = (index % 2) == 0;
			odd = !even;

			return obj;
		}
	}

	private final Iterable target;

	public IterableWithVariables(Iterable target) {
		this.target = target;
	}

	@Override
	public Iter iterator() {
		return new Iter(target.iterator());
	}
}
