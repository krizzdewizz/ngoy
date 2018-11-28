package ngoy.router;

import static ngoy.core.Provider.useValue;

import ngoy.core.ModuleWithProviders;
import ngoy.core.NgModule;
import ngoy.router.internal.OutletComponent;

@NgModule(declarations = { OutletComponent.class, RouterLinkDirective.class }, providers = { Router.class, RouteParams.class })
public final class RouterModule {
	private RouterModule() {
	}

	public static ModuleWithProviders<RouterModule> forRoot(RouterConfig config) {
		return ModuleWithProviders.<RouterModule>of(RouterModule.class)
				.providers(useValue(RouterConfig.class, config), config.getLocationProvider())
				.declarations(routeComponents(config))
				.build();
	}

	private static Class<?>[] routeComponents(RouterConfig config) {
		return config.getRoutes()
				.stream()
				.map(Route::getComponent)
				.toArray(Class[]::new);
	}
}
