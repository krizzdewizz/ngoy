package org.ngoy.routing;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.ngoy.Ngoy;
import org.ngoy.core.Provider;
import org.ngoy.core.TemplateCache;
import org.ngoy.router.Location;
import org.ngoy.router.RouterConfig;
import org.ngoy.router.RouterModule;
import org.ngoy.routing.home.HomeComponent;
import org.ngoy.routing.settings.SettingsComponent;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;

//@Controller
//@RequestMapping("/router/*")
public class RouterMain implements InitializingBean {

	private Ngoy ngoy;

	@Autowired
	private HttpServletRequest request;

	@GetMapping()
	public void home(HttpServletResponse response) throws Exception {
		ngoy.render(response.getOutputStream());
	}

	@Override
	public void afterPropertiesSet() throws Exception {

		// do not disable in production
		TemplateCache.DEFAULT.setDisabled(true);

		RouterConfig routerConfig = RouterConfig //
				.baseHref("/router")
				.location(Provider.useValue(Location.class, () -> request.getRequestURI()))
				.route("home", HomeComponent.class)
				.route("settings", SettingsComponent.class)
				.build();

		ngoy = Ngoy.app(RouterApp.class)
				.modules(RouterModule.forRoot(routerConfig))
				.build();
	}
}