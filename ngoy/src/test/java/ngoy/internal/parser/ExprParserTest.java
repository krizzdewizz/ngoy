package ngoy.internal.parser;

import static java.util.Collections.emptySet;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import org.junit.Test;

import ngoy.internal.parser.ExprParser.ExpressionWithPipesParser;
import ngoy.internal.parser.ExprParser.TextHandler;
import ngoy.internal.parser.org.springframework.expression.CompositeStringExpression;
import ngoy.internal.parser.org.springframework.expression.Expression;
import ngoy.internal.parser.org.springframework.expression.LiteralExpression;
import ngoy.internal.parser.org.springframework.expression.SpelExpression;

public class ExprParserTest {

	@Test
	public void testExpr() {
		TextHandler handler = mock(TextHandler.class);

		ExprParser.parse("a", null, handler);
		verify(handler).text("a", false);
		verifyNoMoreInteractions(handler);

		ExprParser.parse("x{{a}}x", null, handler);
		verify(handler, times(2)).text("x", false);
		verify(handler).text("a", true);
		verifyNoMoreInteractions(handler);
	}

	@Test
	public void testObjLiteral() {
		TextHandler handler = mock(TextHandler.class);

		ExprParser.parse("{{ {} }}", null, handler);
		verify(handler).text("{}", true); // spel trims it
		verifyNoMoreInteractions(handler);
	}

	@Test
	public void testObjLiteral_unsupported_will_lead_to_parse_error_in_spel_engine() {
		TextHandler handler = mock(TextHandler.class);

		ExprParser.parse("{{{}}}", null, handler);
		verify(handler).text("{}", true);
		verifyNoMoreInteractions(handler);
	}

	@Test
	public void testSpelParser() {
		ExpressionWithPipesParser exprParser = new ExpressionWithPipesParser(null);

		CompositeStringExpression e = (CompositeStringExpression) exprParser.parseExpression("a{{'\n'}}b", ExprParser.TEMPLATE_CONTEXT);

		Expression[] exs = e.getExpressions();
		assertThat(exs).hasSize(3);
		assertThat(((LiteralExpression) exs[0]).getExpressionString()).isEqualTo("a");
		assertThat(((SpelExpression) exs[1]).getExpressionString()).isEqualTo("'\n'");
		assertThat(((LiteralExpression) exs[2]).getExpressionString()).isEqualTo("b");
	}

	@Test
	public void test() throws Exception {

		String expr = "name.qbert.toLowerCase(locale == null ? defaultLocale : locale)";

		String code = ExprParser.prefixName(expr, "_cmp", emptySet());
		System.out.println(code);
		assertThat(code).isEqualTo("_cmp.name.qbert.toLowerCase(_cmp.locale == null ? _cmp.defaultLocale : _cmp.locale)");
	}
}
