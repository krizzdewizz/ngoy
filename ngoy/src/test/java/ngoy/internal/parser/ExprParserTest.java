package ngoy.internal.parser;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import org.junit.Test;
import org.springframework.expression.Expression;
import org.springframework.expression.common.CompositeStringExpression;
import org.springframework.expression.common.LiteralExpression;
import org.springframework.expression.spel.standard.SpelExpression;
import org.springframework.expression.spel.standard.SpelExpressionParser;

import ngoy.internal.parser.ExprParser.TextHandler;

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
		SpelExpressionParser exprParser = new SpelExpressionParser();

		CompositeStringExpression e = (CompositeStringExpression) exprParser.parseExpression("a{{'\n'}}b", ExprParser.TEMPLATE_CONTEXT);
		String value = (String) e.getValue();
		assertThat(value).isEqualTo("a\nb");

		Expression[] exs = e.getExpressions();
		assertThat(exs).hasSize(3);
		assertThat(((LiteralExpression) exs[0]).getExpressionString()).isEqualTo("a");
		assertThat(((SpelExpression) exs[1]).getExpressionString()).isEqualTo("'\n'");
		assertThat(((LiteralExpression) exs[2]).getExpressionString()).isEqualTo("b");
	}
}
