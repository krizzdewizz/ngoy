package ngoy.core;

import ngoy.core.internal.Ctx;
import ngoy.core.internal.MinimalEnv;

/**
 * The context or 'model' associated with a simple template.
 * <p>
 * Bindings will be looked up either on the model or on variables.
 * 
 * @author krizz
 */
public final class Context {

	/**
	 * Returns an empty context.
	 * 
	 * @return Context
	 */
	public static Context of() {
		return new Context(Ctx.of(null, MinimalEnv.INJECTOR));
	}

	/**
	 * Returns a context for the given model.
	 * 
	 * @param model Model
	 * @return Context
	 */
	public static Context of(Object model) {
		return new Context(Ctx.of(model, MinimalEnv.INJECTOR));
	}

	/**
	 * Returns a context initialized with the given variable.
	 * 
	 * @param variableName  Name of the variable
	 * @param variableValue Value of the variable
	 * @return Context
	 */
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
	 * @param variableValue may be null
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
