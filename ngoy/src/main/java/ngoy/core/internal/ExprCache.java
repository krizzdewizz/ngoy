package ngoy.core.internal;

import java.util.HashMap;
import java.util.Map;

import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;

public class ExprCache {
	private final Map<String, Expression> cache = new HashMap<>();

	public Expression get(String expr, ExpressionParser parser) {
		Expression expression;
		if ((expression = cache.get(expr)) != null) {
			return expression;
		}

		expression = parser.parseExpression(expr);
		cache.put(expr, expression);
		return expression;
	}
}
