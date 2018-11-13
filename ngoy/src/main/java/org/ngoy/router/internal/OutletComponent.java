package org.ngoy.router.internal;

import static java.lang.String.format;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.ngoy.core.Component;
import org.ngoy.core.Inject;
import org.ngoy.core.OnCompile;
import org.ngoy.core.OnInit;
import org.ngoy.router.Route;
import org.ngoy.router.Router;

@Component(selector = "router-outlet", template = "<ng-content scope></ng-content>")
public class OutletComponent implements OnCompile, OnInit {
	@Inject
	public Router router;

	public int activeRoute;

	@Override
	public void ngOnInit() {
		activeRoute = router.getActiveRoute();
	}

	@Override
	public void ngOnCompile(Element el, String cmpClass) {
		Document doc = el.ownerDocument();

		int i = 0;
		for (Route route : router.getRoutes()) {
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
