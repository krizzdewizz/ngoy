package ngoy.core;

import java.util.HashMap;
import java.util.Map;

import static java.util.Objects.requireNonNull;

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
        return new Context<>(null, null, new HashMap<>());
    }

    /**
     * Returns a context for the given model.
     * <p>
     * Uses {@link Object#getClass()} for the model class.
     *
     * @param model Model
     * @return Context
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    public static <T> Context<T> of(T model) {
        requireNonNull(model, "Model must not be null");
        return new Context(model.getClass(), model, new HashMap<>());
    }

    /**
     * Returns a context for the given model.
     *
     * @param modelClass Class of the model
     * @param model      Model
     * @return Context
     */
    public static <T> Context<T> of(Class<T> modelClass, T model) {
        return new Context<>(modelClass, requireNonNull(model, "Model must not be null"), new HashMap<>());
    }

    /**
     * Returns a context initialized with the given variable.
     * <p>
     * Uses {@link Object#getClass()} for the variable type.
     *
     * @param variableName  Name of the variable
     * @param variableValue Value of the variable
     * @return Context
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    public static Context<?> of(String variableName, Object variableValue) {
        requireNonNull(variableValue, "Variable value must not be null");
        Context ctx = Context.of();
        return ctx.variable(variableName, variableValue.getClass(), variableValue);
    }

    /**
     * Returns a context initialized with the given variable.
     *
     * @param variableName  Name of the variable
     * @param variableType  Type of the variable
     * @param variableValue Value of the variable
     * @return Context
     */
    public static <V> Context<?> of(String variableName, Class<V> variableType, @Nullable V variableValue) {
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
     * <p>
     * Uses {@link Object#getClass()} for the variable type.
     *
     * @param variableName  Name of the variable
     * @param variableValue must not be null
     * @return this
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    public Context<T> variable(String variableName, Object variableValue) {
        requireNonNull(variableValue, "Variable value must not be null");
        variables.put(variableName, new Variable(variableValue.getClass(), variableValue));
        return this;
    }

    /**
     * Adds a variable.
     *
     * @param variableName  Name of the variable
     * @param variableType  Type of the variable
     * @param variableValue may be null
     * @return this
     */
    public <V> Context<T> variable(String variableName, Class<V> variableType, @Nullable V variableValue) {
        variables.put(variableName, new Variable<>(variableType, variableValue));
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
