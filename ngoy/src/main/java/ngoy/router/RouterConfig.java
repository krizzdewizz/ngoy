package ngoy.router;

import ngoy.core.NgoyException;
import ngoy.core.Provider;

import java.util.ArrayList;
import java.util.List;

/**
 * Configuration for the router.
 * <p>
 * Start building the configuration with {@link #baseHref(String)}.
 *
 * @author krizz
 */
public class RouterConfig {

    public static class Builder {

        private final List<Route> routes = new ArrayList<>();
        private String baseHref;
        private Provider locationProvider;

        public Builder location(Provider locationProvider) {
            this.locationProvider = locationProvider;
            return this;
        }

        public Builder route(String path, Class<?> component) {
            routes.add(Route.of(path, component));
            return this;
        }

        public Builder baseHref(String baseHref) {
            this.baseHref = baseHref;
            return this;
        }

        public RouterConfig build() {
            if (locationProvider == null) {
                throw new NgoyException("location provider is not set. Missing call to %s.location()", RouterConfig.class.getName());
            }
            return new RouterConfig(baseHref, locationProvider, routes);
        }
    }

    public static Builder baseHref(String baseHref) {
        return new Builder().baseHref(baseHref);
    }

    private static String checkHref(String baseHref) {
        if (baseHref == null || baseHref.isEmpty()) {
            throw new NgoyException("Router configuration 'baseHref' must be set");
        } else if (baseHref.charAt(0) != '/') {
            throw new NgoyException("Router configuration 'baseHref' must start with a /");
        }
        return baseHref;
    }

    private final String baseHref;
    private final List<Route> routes;
    private final Provider locationProvider;

    private RouterConfig(String baseHref, Provider locationProvider, List<Route> routes) {
        this.locationProvider = locationProvider;
        this.baseHref = checkHref(baseHref);
        this.routes = routes;
    }

    public List<Route> getRoutes() {
        return routes;
    }

    public String getBaseHref() {
        return baseHref;
    }

    public Provider getLocationProvider() {
        return locationProvider;
    }
}
