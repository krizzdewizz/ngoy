package ngoy.internal.parser.org.springframework.expression;

public class Expression {
	public final ExpressionType type;
	public final String string;
	public final Expression[] expressions;

	public Expression(ExpressionType type, String string) {
		this(type, string, null);
	}

	public Expression(ExpressionType type, String string, Expression[] expressions) {
		this.type = type;
		this.string = string;
		this.expressions = expressions;
	}
}
