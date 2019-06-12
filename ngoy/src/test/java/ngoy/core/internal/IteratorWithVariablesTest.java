package ngoy.core.internal;

import ngoy.core.NgoyException;
import ngoy.internal.parser.ForOfVariable;
import org.junit.Test;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;

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

	@Test
	public void testBoolean() {
		IteratorWithVariables itr = new IteratorWithVariables(new boolean[] { true, false });
		assertThat(itr.next()).isEqualTo(true);
		assertThat(itr.next()).isEqualTo(false);
		assertThat(itr.hasNext()).isEqualTo(false);
	}

	@Test
	public void testByte() {
		IteratorWithVariables itr = new IteratorWithVariables(new byte[] { (byte) 1, (byte) 2 });
		assertThat(itr.next()).isEqualTo((byte) 1);
		assertThat(itr.next()).isEqualTo((byte) 2);
		assertThat(itr.hasNext()).isEqualTo(false);
	}

	@Test
	public void testChar() {
		IteratorWithVariables itr = new IteratorWithVariables(new char[] { 'a', 'b' });
		assertThat(itr.next()).isEqualTo('a');
		assertThat(itr.next()).isEqualTo('b');
		assertThat(itr.hasNext()).isEqualTo(false);
	}

	@Test
	public void testShort() {
		IteratorWithVariables itr = new IteratorWithVariables(new short[] { 2, 3 });
		assertThat(itr.next()).isEqualTo((short) 2);
		assertThat(itr.next()).isEqualTo((short) 3);
		assertThat(itr.hasNext()).isEqualTo(false);
	}

	@Test
	public void testInt() {
		IteratorWithVariables itr = new IteratorWithVariables(new int[] { 1, 2 });
		assertThat(itr.next()).isEqualTo(1);
		assertThat(itr.next()).isEqualTo(2);
		assertThat(itr.hasNext()).isEqualTo(false);
	}

	@Test
	public void testLong() {
		IteratorWithVariables itr = new IteratorWithVariables(new long[] { 1l, 2l });
		assertThat(itr.next()).isEqualTo(1l);
		assertThat(itr.next()).isEqualTo(2l);
		assertThat(itr.hasNext()).isEqualTo(false);
	}

	@Test
	public void testFloat() {
		IteratorWithVariables itr = new IteratorWithVariables(new float[] { 1f, 2f });
		assertThat(itr.next()).isEqualTo(1f);
		assertThat(itr.next()).isEqualTo(2f);
		assertThat(itr.hasNext()).isEqualTo(false);
	}

	@Test
	public void testDouble() {
		IteratorWithVariables itr = new IteratorWithVariables(new double[] { 1d, 2d });
		assertThat(itr.next()).isEqualTo(1d);
		assertThat(itr.next()).isEqualTo(2d);
		assertThat(itr.hasNext()).isEqualTo(false);
	}

	@Test(expected = NgoyException.class)
	public void testNull() {
		new IteratorWithVariables((double[]) null);
	}
}
