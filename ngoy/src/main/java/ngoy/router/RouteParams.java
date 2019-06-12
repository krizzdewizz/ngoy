package ngoy.router;

import ngoy.core.NgoyException;
import ngoy.core.Nullable;

import java.util.HashMap;
import java.util.Map;

/**
 * The resolved router parameters.
 * <p>
 * Inject it where you need it.
 *
 * @author krizz
 */
public class RouteParams {

    private final Map<String, String> params = new HashMap<>();

    /**
     * Returns the parameter for the given name, throwing an exception if there is
     * no such parameter.
     *
     * @param name Name of the parameter
     * @return Value
     * @throws NgoyException if there is no such parameter
     */
    public String get(String name) {
        String value = getOrNull(name);
        if (value == null) {
            throw new NgoyException("Router error: parameter '%s' could not be found", name);
        }
        return value;
    }

    /**
     * Returns the parameter for the given name or null if there is no such
     * parameter.
     *
     * @param name Name of the parameter
     * @return Value or null if there is no such parameter
     */
    @Nullable
    public String getOrNull(String name) {
        return params.get(name);
    }

    /**
     * Puts a parameter.
     *
     * @param name  Name of the parameter
     * @param value Value of the parameter
     * @return this
     */
    public RouteParams put(String name, String value) {
        params.put(name, value);
        return this;
    }

    /**
     * Puts all parameters of the other router params into this parameters.
     *
     * @param other Parameters to copy from
     * @return this
     */
    public RouteParams putAll(RouteParams other) {
        params.putAll(other.params);
        return this;
    }

    /**
     * Clears all parameters.
     */
    public void clear() {
        params.clear();
    }
}
