package ngoy.router;

import java.util.List;
import java.util.function.Consumer;

import ngoy.core.Inject;

/**
 * The router determines the active route based on the current location and
 * configured routes.
 * 
 * @author krizz
 */
public class Router {

	@Inject
	public Location location;

	@Inject
	public RouterConfig config;

	private Integer activeRouteOverride;

	public boolean isActive(Route route) {
		return getRoutes().indexOf(route) == getActiveRoute();
	}

	public void withActivatedRoutesDo(Consumer<Route> runnable) {
		try {
			activeRouteOverride = 0;
			for (Route route : config.getRoutes()) {
				runnable.accept(route);
				activeRouteOverride++;
			}
		} finally {
			activeRouteOverride = null;
		}
	}

	public int getActiveRoute() {
		if (activeRouteOverride != null) {
			return activeRouteOverride;
		}

		String base = config.getBaseHref();
		String path = location.getPath();

		if (base.length() > path.length() || !path.startsWith(base)) {
			return 0;
		}

		String sub = path.substring(base.length());
		if (sub.isEmpty()) {
			return 0;
		}

		if (!base.equals("/")) {
			sub = sub.substring(1); // remove slash
		}

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
