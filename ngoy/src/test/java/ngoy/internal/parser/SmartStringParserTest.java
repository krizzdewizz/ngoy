package ngoy.internal.parser;

import static ngoy.internal.parser.SmartStringParser.toJavaString;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.CoreMatchers.containsString;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class SmartStringParserTest {

	@Rule
	public ExpectedException expectedEx = ExpectedException.none();

	private void assertParse(String code, String expected) throws Exception {
//		ExpressionEvaluator e = new ExpressionEvaluator();
//		e.cook(code);
//		String evaled = (String) e.evaluate(new Object[0]);

//		ExpressionEvaluator eExpected = new ExpressionEvaluator();
//		eExpected.cook(expected);
//		String evaledExpected = (String) eExpected.evaluate(new Object[0]);

		assertThat(code).isEqualTo(expected);
//		assertThat(evaled).isEqualTo(evaledExpected);
	}

	@Test
	public void nullEmpty() {
		assertThat(toJavaString(null)).isNull();
		assertThat(toJavaString("")).isEqualTo("");
	}

	@Test
	public void backslash() {
		assertThat(toJavaString("'\\'")).isEqualTo("\"\\\"");
	}

	@Test
	public void java() throws Exception {
		assertThat(toJavaString("\"\"")).isEqualTo("\"\"");
		assertThat(toJavaString("\"'\"")).isEqualTo("\"'\"");
		assertThat(toJavaString("\"\\\"\"")).isEqualTo("\"\\\"\"");
		assertParse(toJavaString("'\"'"), "\"\\\"\"");
	}

	@Test
	public void simple() throws Exception {
		assertParse(toJavaString("''"), "\"\"");
		assertParse(toJavaString("'a'"), "\"a\"");
	}

	@Test
	public void malformed() {
		expectedEx.expect(ParseException.class);
		expectedEx.expectMessage(containsString("not properly closed"));
		expectedEx.expectMessage(containsString("single-quote"));
		toJavaString("'");
	}

	@Test
	public void malformed2() {
		expectedEx.expect(ParseException.class);
		expectedEx.expectMessage(containsString("not properly closed"));
		expectedEx.expectMessage(containsString("single-quote"));
		toJavaString("a'");
	}

	@Test
	public void malformed3() {
		expectedEx.expect(ParseException.class);
		expectedEx.expectMessage(containsString("not properly closed"));
		expectedEx.expectMessage(containsString("double-quote"));
		toJavaString("\"");
	}

	@Test
	public void singleQuote() throws Exception {
		assertParse(toJavaString("'\\''"), "\"\\\"\"");
	}

	@Test
	public void code() throws Exception {
		assertParse(toJavaString("'b'.equals('a') ? 'x' : 'z'"), "\"b\".equals(\"a\") ? \"x\" : \"z\"");
		assertParse(toJavaString("'\\'a'"), "\"\\\"a\"");
	}
}
