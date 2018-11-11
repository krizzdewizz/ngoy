package org.ngoy.router.outlet;

import static java.lang.String.format;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.ngoy.core.Component;
import org.ngoy.core.ElementRef;
import org.ngoy.core.Inject;
import org.ngoy.core.OnCompile;
import org.ngoy.core.OnInit;
import org.ngoy.router.Config;
import org.ngoy.router.Route;
import org.ngoy.router.RouterService;

@Component(selector = "router-outlet", template = "<ng-content scope></ng-content>")
public class OutletComponent implements OnCompile, OnInit {

	@Inject
	public Config config;

	@Inject
	public RouterService routerService;

	public int activeRoute;

	@Override
	public void ngOnInit() {
		activeRoute = routerService.getActiveRoute();
	}

	@Override
	public void ngOnCompile(ElementRef elRef, String cmpClass) {
		Element el = (Element) elRef.getNativeElement();

		Document doc = el.ownerDocument();

		int i = 0;
		for (Route route : config.getRoutes()) {
			Element routeEl = doc.createElement(getSelector(route.getComponent()));
			routeEl.attr("*ngIf", format("activeRoute == %s", i));
			el.appendChild(routeEl);
			i++;
		}
	}

	private String getSelector(Class<?> component) {
		return component.getAnnotation(Component.class)
				.selector();
	}

}
