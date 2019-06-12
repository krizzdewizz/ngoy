package ngoy.router;

import ngoy.core.Inject;
import ngoy.core.NgoyException;

import java.util.List;
import java.util.function.Consumer;

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

    @Inject
    public RouteParams routeParams;

    @Inject
    public ActiveRouteProvider activeRouteProvider;

    private Integer activeRouteOverride;

    /**
     * Returns whether the given route is the active one.
     *
     * @param route Route to test
     * @return true if active
     */
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
                    ActiveRoute route = activeRouteProvider.getActiveRoute(path);
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

    /**
     * Returns the active route index based on {@link Location#getPath()}.
     *
     * @return active route index or 0 if none found
     */
    public int getActiveRoute() {
        if (activeRouteOverride != null) {
            return activeRouteOverride;
        }

        ActiveRoute route = activeRouteProvider.getActiveRoute(location.getPath());
        if (route != null) {
            routeParams.putAll(route.params);
            return route.index;
        }

        return 0;
    }

    /**
     * Returns all configured routes.
     *
     * @return All configured routes
     */
    public List<Route> getRoutes() {
        return config.getRoutes();
    }

    /**
     * Clears the route parameters.
     */
    public void clearRouteParams() {
        routeParams.clear();
    }

    private boolean hasParams(Route route) {
        return route.getPath().contains("/:");
    }
}
