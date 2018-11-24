package ngoy.core;

import java.util.HashMap;
import java.util.Map;

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
		return new Context(null, new HashMap<>());
	}

	/**
	 * Returns a context for the given model.
	 * 
	 * @param model Model
	 * @return Context
	 */
	public static Context of(Object model) {
		return new Context(model, new HashMap<>());
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

	private final Object model;
	private final Map<String, Object> variables;

	private Context(Object model, Map<String, Object> variables) {
		this.model = model;
		this.variables = variables;
	}

	/**
	 * Adds a variable.
	 * 
	 * @param variableName
	 * @param variableValue may be null
	 * @return this
	 */
	public Context variable(String variableName, Object variableValue) {
		variables.put(variableName, variableValue);
		return this;
	}

	public Object getModel() {
		return model;
	}

	public Map<String, Object> getVariables() {
		return variables;
	}
}
