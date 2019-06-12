package ngoy.core;

/**
 * Transforms an input value to an output value.
 * <p>
 * The transformation can be parametrized. Every pipe must implement this
 * interface.
 *
 * @author krizz
 */
public interface PipeTransform {
    /**
     * @param obj if null, implementors should return null
     */
    @Nullable
    Object transform(@Nullable Object obj, Object... params);
}
