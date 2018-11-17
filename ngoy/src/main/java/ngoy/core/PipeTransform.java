package ngoy.core;

public interface PipeTransform {
	/**
	 * @param obj if null, implementors should return null
	 */
	@Nullable
	Object transform(@Nullable Object obj, Object... params);
}
