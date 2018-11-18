package ngoy.routing;

import java.nio.file.Paths;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import ngoy.Ngoy;
import ngoy.core.Provider;
import ngoy.core.TemplateCache;
import ngoy.router.Location;
import ngoy.router.RouterConfig;
import ngoy.router.RouterModule;
import ngoy.routing.home.HomeComponent;
import ngoy.routing.settings.SettingsComponent;

@Controller
@RequestMapping("/router/*")
public class RouterMain implements InitializingBean {

	private Ngoy ngoy;

	@Autowired
	private HttpServletRequest request;

	@GetMapping()
	public void home(HttpServletResponse response) throws Exception {
		//ngoy.renderSite(Paths.get("d:/downloads/abc"));

		ngoy.render(response.getOutputStream());
	}

	@Override
	public void afterPropertiesSet() throws Exception {

		// do not disable in production
		TemplateCache.DEFAULT.setDisabled(true);

		RouterConfig routerConfig = RouterConfig //
				.baseHref("/router")
				.location(Provider.useValue(Location.class, () -> request.getRequestURI()))
				.route("index", HomeComponent.class)
				.route("settings", SettingsComponent.class)
				.build();

		ngoy = Ngoy.app(RouterApp.class)
				.modules(RouterModule.forRoot(routerConfig))
				.build();
	}
}