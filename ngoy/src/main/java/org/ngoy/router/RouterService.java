package org.ngoy.router;

import java.util.List;

import org.ngoy.core.Inject;

public class RouterService {

	@Inject
	public Location location;

	@Inject
	public Config config;

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

		List<Route> routes = config.getRoutes();
		for (int i = 0, n = routes.size(); i < n; i++) {
			if (routes.get(i)
					.getPath()
					.equals(sub)) {
				return i;
			}
		}

		return 0;
	}
}
