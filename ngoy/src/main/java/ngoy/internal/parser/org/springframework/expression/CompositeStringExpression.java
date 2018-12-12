/*
 * Copyright 2002-2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ngoy.internal.parser.org.springframework.expression;

/**
 * Represents a template expression broken into pieces. Each piece will be an
 * Expression but pure text parts to the template will be represented as
 * LiteralExpression objects. An example of a template expression might be:
 *
 * <pre class="code">
 * &quot;Hello ${getName()}&quot;
 * </pre>
 *
 * which will be represented as a CompositeStringExpression of two parts. The
 * first part being a LiteralExpression representing 'Hello ' and the second
 * part being a real expression that will call {@code getName()} when invoked.
 *
 * @author Andy Clement
 * @author Juergen Hoeller
 * @since 3.0
 */
public class CompositeStringExpression implements Expression {

	private final String expressionString;

	/** The array of expressions that make up the composite expression. */
	private final Expression[] expressions;

	public CompositeStringExpression(String expressionString, Expression[] expressions) {
		this.expressionString = expressionString;
		this.expressions = expressions;
	}

	public final String getExpressionString() {
		return this.expressionString;
	}

	public final Expression[] getExpressions() {
		return this.expressions;
	}
}
