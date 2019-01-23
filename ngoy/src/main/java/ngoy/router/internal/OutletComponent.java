package ngoy.router.internal;

import static ngoy.core.dom.XDom.appendChild;
import static ngoy.core.dom.XDom.createElement;

import jodd.jerry.Jerry;
import ngoy.core.Component;
import ngoy.core.Inject;
import ngoy.core.OnCompile;
import ngoy.core.OnDestroy;
import ngoy.core.OnInit;
import ngoy.router.Route;
import ngoy.router.Router;

@Component(selector = "router-outlet", template = "<ng-content scope></ng-content>")
public class OutletComponent implements OnCompile, OnInit, OnDestroy {
	@Inject
	public Router router;

	public int activeRoute;

	@Override
	public void ngOnInit() {
		activeRoute = router.getActiveRoute();
	}

	@Override
	public void ngOnDestroy() {
		router.clearRouteParams();
	}

	@Override
	public void ngOnCompile(Jerry el, String componentClass) {
		Jerry container = appendChild(el, createElement("ng-container", el));
		int i = 0;
		container.attr("[ngSwitch]", "activeRoute");
		for (Route route : router.getRoutes()) {
			Jerry tpl = appendChild(container, createElement("ng-template", container));
			tpl.attr("[ngSwitchCase]", String.valueOf(i));
			appendChild(tpl, createElement(getSelector(route.getComponent()), tpl));
			i++;
		}
	}

	private String getSelector(Class<?> component) {
		return component.getAnnotation(Component.class)
				.selector();
	}
}
