package org.ngoy.parser;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;
import static org.hamcrest.CoreMatchers.containsString;

import java.util.Map;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.ngoy.core.NgoyException;
import org.ngoy.internal.parser.ForOfVariable;

public class ForOfVariableTest {

	@Test
	public void testNone() {
		assertThat(ForOfVariable.parse("let p of persons;")).isEmpty();
	}

	@Test
	public void testIndex() {
		Map<ForOfVariable, String> map = ForOfVariable.parse("let p of persons;  index as ii ");
		assertThat(map).size()
				.isEqualTo(1);
		assertThat(map).contains(entry(ForOfVariable.index, "ii"));
	}

	@Test
	public void testIndexOdd() {
		Map<ForOfVariable, String> map = ForOfVariable.parse("let p of persons;  index as ii ; odd as o");
		assertThat(map).size()
				.isEqualTo(2);
		assertThat(map).contains(entry(ForOfVariable.index, "ii"));
		assertThat(map).contains(entry(ForOfVariable.odd, "o"));
	}

	@Rule
	public ExpectedException expectedEx = ExpectedException.none();

	@Test
	public void testUnknownVariable() {
		expectedEx.expect(NgoyException.class);
		expectedEx.expectMessage(containsString("Unknown ngFor variable"));
		expectedEx.expectMessage(containsString("unk"));
		ForOfVariable.parse("let p of persons;  unk as ii");
	}

	@Test
	public void testParseError() {
		expectedEx.expect(NgoyException.class);
		expectedEx.expectMessage(containsString("Parse error in ngFor"));
		ForOfVariable.parse("let p of persons;  index = ii");
	}
}
