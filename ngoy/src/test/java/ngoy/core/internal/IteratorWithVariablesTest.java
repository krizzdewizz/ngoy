package ngoy.core.internal;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import ngoy.internal.parser.ForOfVariable;

public class IteratorWithVariablesTest {
	@Test
	public void test() {

		List<String> all = asList("a", "b", "c");

		Map<ForOfVariable, String> vars = new EnumMap<>(ForOfVariable.class);
		vars.put(ForOfVariable.index, "ii");
		vars.put(ForOfVariable.first, "ff");
		vars.put(ForOfVariable.last, "ll");
		vars.put(ForOfVariable.odd, "oo");
		vars.put(ForOfVariable.even, "ee");

		IteratorWithVariables iter = new IteratorWithVariables(all);
		assertThat(iter.index).isEqualTo(-1);
		iter.next();
		assertThat(iter.index).isEqualTo(0);
		assertThat(iter.even).isEqualTo(true);
		assertThat(iter.odd).isEqualTo(false);
		assertThat(iter.first).isEqualTo(true);
		assertThat(iter.last).isEqualTo(false);

		iter.next();
		assertThat(iter.index).isEqualTo(1);
		assertThat(iter.even).isEqualTo(false);
		assertThat(iter.odd).isEqualTo(true);
		assertThat(iter.first).isEqualTo(false);
		assertThat(iter.last).isEqualTo(false);

		iter.next();
		assertThat(iter.index).isEqualTo(2);
		assertThat(iter.even).isEqualTo(true);
		assertThat(iter.odd).isEqualTo(false);
		assertThat(iter.first).isEqualTo(false);
		assertThat(iter.last).isEqualTo(true);
	}
}
