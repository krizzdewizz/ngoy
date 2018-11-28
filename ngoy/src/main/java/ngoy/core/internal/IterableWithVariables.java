package ngoy.core.internal;

import static ngoy.core.Util.isSet;

import java.util.Iterator;
import java.util.Map;

import ngoy.internal.parser.ForOfVariable;

@SuppressWarnings("rawtypes")
public class IterableWithVariables implements Iterable {

	public interface IterVariable {
		void iterVariable(String variable, Object value);
	}

	private class Iter implements Iterator {

		private final Iterator targetIter;

		private int index;
		private boolean first;
		private boolean last;
		private boolean even;
		private boolean odd;

		public Iter(Iterator targetIter) {
			this.targetIter = targetIter;
			index = -1;
		}

		void notifyVariable(ForOfVariable v, Object value) {
			String alias = variables.get(v);
			if (isSet(alias)) {
				iterVariable.iterVariable(alias, value);
			}
		}

		void notifyVariables() {
			notifyVariable(ForOfVariable.index, index);
			notifyVariable(ForOfVariable.first, first);
			notifyVariable(ForOfVariable.last, last);
			notifyVariable(ForOfVariable.even, even);
			notifyVariable(ForOfVariable.odd, odd);
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

			notifyVariables();

			return obj;
		}

	}

	private final Iterable target;
	private final Map<ForOfVariable, String> variables;
	private final IterVariable iterVariable;

	public IterableWithVariables(Iterable target, Map<ForOfVariable, String> variables, IterVariable iterVariable) {
		this.target = target;
		this.variables = variables;
		this.iterVariable = iterVariable;
	}

	@Override
	public Iterator iterator() {
		return new Iter(target.iterator());
	}
}
