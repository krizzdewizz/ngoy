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
 * A very simple hardcoded implementation of the Expression interface that
 * represents a string literal. It is used with CompositeStringExpression when
 * representing a template expression which is made up of pieces - some being
 * real expressions to be handled by an EL implementation like SpEL, and some
 * being just textual elements.
 *
 * @author Andy Clement
 * @author Juergen Hoeller
 * @since 3.0
 */
public class LiteralExpression implements Expression {

	/** Fixed literal value of this expression. */
	private final String literalValue;

	public LiteralExpression(String literalValue) {
		this.literalValue = literalValue;
	}

	public final String getExpressionString() {
		return this.literalValue;
	}
}
