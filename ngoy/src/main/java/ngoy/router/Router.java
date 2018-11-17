package ngoy.router;

import java.util.List;

import ngoy.core.Inject;

public class Router {

	@Inject
	public Location location;

	@Inject
	public RouterConfig config;

	public boolean isActive(Route route) {
		return getRoutes().indexOf(route) == getActiveRoute();
	}

	public int getActiveRoute() {
		String base = config.getBaseHref();
		String path = location.getPath();

		if (base.length() > path.length() || !path.startsWith(base)) {
			return 0;
		}

		String sub = path.substring(base.length());
		if (sub.isEmpty()) {
			return 0;
		}

		sub = sub.substring(1); // remove slash

		List<Route> routes = getRoutes();
		for (int i = 0, n = routes.size(); i < n; i++) {
			if (routes.get(i)
					.getPath()
					.equals(sub)) {
				return i;
			}
		}

		return 0;
	}

	public List<Route> getRoutes() {
		return config.getRoutes();
	}
}
