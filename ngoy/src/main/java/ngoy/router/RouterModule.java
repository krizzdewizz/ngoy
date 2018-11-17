package ngoy.router;

import static java.util.stream.Collectors.toList;
import static ngoy.core.Provider.useValue;

import ngoy.core.ModuleWithProviders;
import ngoy.core.NgModule;
import ngoy.router.internal.OutletComponent;

@NgModule(declarations = { OutletComponent.class, RouterLinkDirective.class }, providers = { Router.class })
public class RouterModule {

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
				.collect(toList())
				.toArray(new Class<?>[0]);
	}
}
