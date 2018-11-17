package org.ngoy.router.internal;

import static java.lang.String.format;
import static org.ngoy.internal.parser.visitor.XDom.appendChild;
import static org.ngoy.internal.parser.visitor.XDom.createElement;

import org.ngoy.core.Component;
import org.ngoy.core.Inject;
import org.ngoy.core.OnCompile;
import org.ngoy.core.OnInit;
import org.ngoy.router.Route;
import org.ngoy.router.Router;

import jodd.jerry.Jerry;

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
	public void ngOnCompile(Jerry el, String cmpClass) {
		int i = 0;
		for (Route route : router.getRoutes()) {
			Jerry routeEl = createElement(getSelector(route.getComponent()));
			routeEl.attr("*ngIf", format("activeRoute == %s", i));
			appendChild(el, routeEl);
			i++;
		}
	}

	private String getSelector(Class<?> component) {
		return component.getAnnotation(Component.class)
				.selector();
	}

}
