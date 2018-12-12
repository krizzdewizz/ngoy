package ngoy.internal.parser.org.springframework.expression;

public class SpelExpression implements Expression {

	private final String expression;

	public SpelExpression(String expression) {
		this.expression = expression;
	}

	public final String getExpressionString() {
		return this.expression;
	}
}
