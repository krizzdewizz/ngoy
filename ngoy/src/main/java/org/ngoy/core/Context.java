package org.ngoy.core;

import org.ngoy.core.internal.Ctx;

/**
 * The context or 'model' associated with a template.
 * <p>
 * Bindings will be looked up either on the 'modelRoot' or on variables.
 * 
 * @author krizz
 */
public final class Context {

	public static Context of() {
		return new Context(Ctx.of());
	}

	public static Context of(Object modelRoot) {
		return new Context(Ctx.of(modelRoot));
	}

	public static Context of(String variableName, Object variableValue) {
		return of().variable(variableName, variableValue);
	}

	private final Ctx ctx;

	private Context(Ctx ctx) {
		this.ctx = ctx;
	}

	/**
	 * Adds a variable.
	 * 
	 * @param variableName
	 * @param variableValue maybe null
	 * @return this
	 */
	public Context variable(String variableName, Object variableValue) {
		ctx.variable(variableName, variableValue);
		return this;
	}

	/**
	 * non-api
	 */
	public Object internal() {
		return ctx;
	}
}
