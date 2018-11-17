package ngoy.core.internal;

import static java.util.Arrays.asList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import ngoy.core.internal.IterableWithVariables;
import ngoy.core.internal.IterableWithVariables.IterVariable;
import ngoy.internal.parser.ForOfVariable;

public class IterableWithVariablesTest {
	@Test
	public void test() {

		List<String> all = asList("a", "b", "c");

		Map<ForOfVariable, String> vars = new EnumMap<>(ForOfVariable.class);
		vars.put(ForOfVariable.index, "ii");
		vars.put(ForOfVariable.first, "ff");
		vars.put(ForOfVariable.last, "ll");
		vars.put(ForOfVariable.odd, "oo");
		vars.put(ForOfVariable.even, "ee");

		IterVariable target = mock(IterVariable.class);
		IterableWithVariables iter = new IterableWithVariables(all, vars, target);

		for (@SuppressWarnings("unused")
		Object it : iter) {
		}

		verify(target).iterVariable("ii", 0);
		verify(target).iterVariable("ii", 1);
		verify(target).iterVariable("ii", 2);

		verify(target).iterVariable("ff", true);
		verify(target, times(2)).iterVariable("ff", false);

		verify(target, times(2)).iterVariable("ll", false);
		verify(target).iterVariable("ll", true);

		verify(target, times(2)).iterVariable("ee", true);
		verify(target, times(2)).iterVariable("oo", false);
		verify(target).iterVariable("oo", true);
		verify(target).iterVariable("ee", false);
		verifyNoMoreInteractions(target);
	}
}
