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
public final class Context<T> {

	/**
	 * Returns an empty context.
	 * 
	 * @return Context
	 */
	public static <T> Context<T> of() {
		return new Context<T>(null, null, new HashMap<>());
	}

	/**
	 * Returns a context for the given model.
	 * 
	 * @param model Model
	 * @return Context
	 */
	public static <T> Context<T> of(Class<T> modelClass, T model) {
		return new Context<T>(modelClass, model, new HashMap<>());
	}

	/**
	 * Returns a context initialized with the given variable.
	 * 
	 * @param variableName  Name of the variable
	 * @param variableValue Value of the variable
	 * @return Context
	 */
	public static <V> Context<?> of(String variableName, Class<V> variableType, V variableValue) {
		return Context.of()
				.variable(variableName, variableType, variableValue);
	}

	private final T model;
	private final Map<String, Variable<?>> variables;
	private final Class<T> modelClass;

	private Context(Class<T> modelClass, T model, Map<String, Variable<?>> variables) {
		this.modelClass = modelClass;
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
	public <V> Context<T> variable(String variableName, Class<V> variableType, V variableValue) {
		variables.put(variableName, new Variable<V>(variableType, variableValue));
		return this;
	}

	public T getModel() {
		return model;
	}

	public Map<String, Variable<?>> getVariables() {
		return variables;
	}

	public Class<T> getModelClass() {
		return modelClass;
	}
}
