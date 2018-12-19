package ngoy.internal.parser;

import static java.util.Collections.emptyMap;
import static ngoy.internal.parser.FieldAccessToGetterParser.fieldAccessToGetter;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.CoreMatchers.containsString;

import java.util.List;
import java.util.Map;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import ngoy.core.NgoyException;
import ngoy.model.Person;

@SuppressWarnings("unused")
public class FieldAccessToGetterParserInTheWildTest {
	private static class MyCmp {
		public int x;
	}

	@Test(expected = NgoyException.class)
	public void some1() {
		fieldAccessToGetter(MyCmp.class, emptyMap(), "int a = 0, c = 9; boolean q = false;", emptyMap(), null);
	}

	@Test(expected = NgoyException.class)
	public void some2() {
		fieldAccessToGetter(MyCmp.class, emptyMap(), "if (false) return true", emptyMap(), null);
	}

	@Test(expected = NgoyException.class)
	public void manyRvalues() {
		fieldAccessToGetter(MyCmp.class, emptyMap(), "0, 9", emptyMap(), null);
	}

	@Test(expected = NgoyException.class)
	public void empty() {
		fieldAccessToGetter(MyCmp.class, emptyMap(), "", emptyMap(), null);
	}

	@Test(expected = NgoyException.class)
	public void nulll() {
		fieldAccessToGetter(MyCmp.class, emptyMap(), null, emptyMap(), null);
	}

	@Test(expected = NgoyException.class)
	public void aclass() {
		fieldAccessToGetter(MyCmp.class, emptyMap(), "class x {}", emptyMap(), null);
	}

	@Test(expected = NgoyException.class)
	public void assignment() {
		fieldAccessToGetter(MyCmp.class, emptyMap(), "x = 0;", emptyMap(), null);
	}

	@Test(expected = NgoyException.class)
	public void incr() {
		fieldAccessToGetter(MyCmp.class, emptyMap(), "x++", emptyMap(), null);
	}

	@Test(expected = NgoyException.class)
	public void decr() {
		fieldAccessToGetter(MyCmp.class, emptyMap(), "x--", emptyMap(), null);
	}

	@Test(expected = NgoyException.class)
	public void thiss() {
		fieldAccessToGetter(MyCmp.class, emptyMap(), "this", emptyMap(), null);
	}

	@Test(expected = NgoyException.class)
	public void anon() {
		assertThat(fieldAccessToGetter(MyCmp.class, emptyMap(), "new Runnable(){ public void run() {}  }", emptyMap(), null)).isEqualTo("");
	}
}
