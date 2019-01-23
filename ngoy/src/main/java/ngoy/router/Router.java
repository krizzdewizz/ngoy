package ngoy.router;

import java.util.List;
import java.util.function.Consumer;

import ngoy.core.Inject;
import ngoy.core.NgoyException;
import ngoy.core.Nullable;

/**
 * The router determines the active route based on the current location and
 * configured routes.
 * 
 * @author krizz
 */
public class Router {

	private static class RouteWithParams {
		private final int index;
		private final RouteParams params;

		private RouteWithParams(int index, RouteParams params) {
			this.index = index;
			this.params = params;
		}
	}

	@Inject
	public Location location;

	@Inject
	public RouterConfig config;

	@Inject
	public RouteParams routeParams;

	private Integer activeRouteOverride;

	public boolean isActive(Route route) {
		return getRoutes().indexOf(route) == getActiveRoute();
	}

	/**
	 * @param paths    if is empty, runs for all configured routes
	 * @param runnable
	 */
	public void withRoutesDo(List<String> paths, Consumer<String> runnable) {
		try {
			if (paths.isEmpty()) {
				activeRouteOverride = 0;
				for (Route route : config.getRoutes()) {
					if (hasParams(route)) {
						continue;
					}
					runnable.accept(route.getPath());
					activeRouteOverride++;
				}
			} else {
				for (String path : paths) {
					RouteWithParams route = findRoute(path);
					if (route == null) {
						throw new NgoyException("Router error: cannot find route for path '%s'", path);
					}
					activeRouteOverride = route.index;
					routeParams.clear();
					routeParams.putAll(route.params);
					runnable.accept(path);
				}
			}
		} finally {
			activeRouteOverride = null;
		}
	}

	private boolean hasParams(Route route) {
		return route.getPath()
				.indexOf("/:") >= 0;
	}

	@Nullable
	private RouteWithParams findRoute(String path) {
		String base = config.getBaseHref();

		if (base.length() > path.length() || !path.startsWith(base)) {
			return null;
		}

		String sub = path.substring(base.length());
		if (sub.isEmpty()) {
			return null;
		}

		if (!base.equals("/")) {
			sub = sub.substring(1); // remove slash
		}

		String[] subSplits = sub.split("/");

		List<Route> routes = getRoutes();
		for (int i = 0, n = routes.size(); i < n; i++) {
			Route route = routes.get(i);
			String[] splits = route.getPath()
					.split("/");

			if (splits[0].equals(subSplits[0])) {
				RouteParams params = new RouteParams();
				if (splits.length > 1) {
					String param = splits[1];
					if (param.startsWith(":")) {
						param = param.substring(1);
					}
					String value = subSplits.length > 1 ? subSplits[1] : null;
					params.put(param, value);
				}

				return new RouteWithParams(i, params);
			}
		}

		return null;
	}

	public int getActiveRoute() {
		if (activeRouteOverride != null) {
			return activeRouteOverride;
		}

		String path = location.getPath();
		RouteWithParams route = findRoute(path);
		if (route != null) {
			routeParams.putAll(route.params);
			return route.index;
		}

		return 0;
	}

	public List<Route> getRoutes() {
		return config.getRoutes();
	}

	public void clearRouteParams() {
		routeParams.clear();
	}
}
