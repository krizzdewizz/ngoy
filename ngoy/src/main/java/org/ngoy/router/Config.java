package org.ngoy.router;

import java.util.ArrayList;
import java.util.List;

import org.ngoy.core.NgoyException;
import org.ngoy.core.Provider;
import org.ngoy.router.Config.Builder.LocationBuilder;

public class Config {

	public static class Builder {

		public class LocationBuilder {
			public RouteBuilder location(Provider locationProvider) {
				Builder.this.locationProvider = locationProvider;
				return new RouteBuilder();
			}
		}

		public class RouteBuilder {
			public Builder route(String path, Class<?> component) {
				return Builder.this.route(path, component);
			}
		}

		private final List<Route> routes = new ArrayList<>();
		private String baseHref;
		private Provider locationProvider;

		public Builder route(String path, Class<?> component) {
			routes.add(Route.of(path, component));
			return this;
		}

		private LocationBuilder baseHref(String baseHref) {
			this.baseHref = baseHref;
			return new LocationBuilder();
		}

		public Config build() {
			return new Config(baseHref, locationProvider, routes);
		}
	}

	public static LocationBuilder baseHref(String baseHref) {
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

	private Config(String baseHref, Provider locationProvider, List<Route> routes) {
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
