package ngoy.router;

/**
 * The currently active route based on {@link Location#getPath()}.
 * 
 * @author krizz
 * @see ActiveRouteProvider
 */
public class ActiveRoute {
	/**
	 * The index within the configured routes.
	 */
	public final int index;

	/**
	 * The params/arguments.
	 */
	public final RouteParams params;

	public ActiveRoute(int index, RouteParams params) {
		this.index = index;
		this.params = params;
	}
}