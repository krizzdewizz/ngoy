package ngoy.routing;

import java.util.List;

import ngoy.core.Component;
import ngoy.core.Inject;
import ngoy.core.NgModule;
import ngoy.router.Route;
import ngoy.router.Router;

@Component(selector = "", templateUrl = "app.component.html")
@NgModule()
public class RouterApp {
	public final String appName = "Router";

	@Inject
	public Router router;

	public List<Route> getRoutes() {
		return router.getRoutes();
	}

	public boolean isActiveRoute(Route route) {
		return router.isActive(route);
	}
}
