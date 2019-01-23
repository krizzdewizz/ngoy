package ngoy.router;

import ngoy.core.Nullable;

/**
 * Returns the active route based on the path returned by
 * {@link Location#getPath()}.
 * <p>
 * You may provide your own.
 * 
 * @author krizz
 */
public interface ActiveRouteProvider {
	/**
	 * Returns the active route based on the path returned by
	 * {@link Location#getPath()}.
	 * 
	 * @param path Path
	 * @return active route or null if none
	 */
	@Nullable
	ActiveRoute getActiveRoute(String path);
}
