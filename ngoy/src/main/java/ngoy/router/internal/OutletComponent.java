package ngoy.router.internal;

import static java.lang.String.format;
import static ngoy.internal.parser.visitor.XDom.appendChild;
import static ngoy.internal.parser.visitor.XDom.createElement;

import jodd.jerry.Jerry;
import ngoy.core.Component;
import ngoy.core.Inject;
import ngoy.core.OnCompile;
import ngoy.core.OnInit;
import ngoy.router.Route;
import ngoy.router.Router;

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
