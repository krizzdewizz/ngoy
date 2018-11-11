package org.ngoy.q;

import java.util.List;

import org.ngoy.core.Component;
import org.ngoy.core.Inject;
import org.ngoy.core.NgModule;
import org.ngoy.q.home.HomeComponent;
import org.ngoy.q.settings.SettingsComponent;
import org.ngoy.router.Route;
import org.ngoy.router.Router;
import org.springframework.stereotype.Controller;

@Component(selector = "", templateUrl = "app.component.html")
@NgModule(declarations = { HomeComponent.class, SettingsComponent.class })
@Controller
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
