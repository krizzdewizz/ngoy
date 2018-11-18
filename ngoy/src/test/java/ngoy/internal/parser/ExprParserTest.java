package ngoy.internal.parser;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import org.junit.Test;

import ngoy.internal.parser.ExprParser.TextHandler;

public class ExprParserTest {

	@Test
	public void testExpr() {
		TextHandler handler = mock(TextHandler.class);

		ExprParser.parse("a", handler);
		verify(handler).text("a", false);
		verifyNoMoreInteractions(handler);

		ExprParser.parse("x{{a}}x", handler);
		verify(handler, times(2)).text("x", false);
		verify(handler).text("a", true);
		verifyNoMoreInteractions(handler);
	}

	@Test
	public void testObjLiteral() {
		TextHandler handler = mock(TextHandler.class);

		ExprParser.parse("{{ {} }}", handler);
		verify(handler).text(" {} ", true);
		verifyNoMoreInteractions(handler);
	}

	@Test
	public void testObjLiteral_unsupported_will_lead_to_parse_error_in_spel_engine() {
		TextHandler handler = mock(TextHandler.class);

		ExprParser.parse("{{{}}}", handler);
		verify(handler).text("{", true);
		verify(handler).text("}", false);
		verifyNoMoreInteractions(handler);
	}
}
