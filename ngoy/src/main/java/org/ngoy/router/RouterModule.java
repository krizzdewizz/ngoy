package org.ngoy.router;

import static java.util.stream.Collectors.toList;
import static org.ngoy.core.Provider.useValue;

import org.ngoy.core.ModuleWithProviders;
import org.ngoy.core.NgModule;
import org.ngoy.router.internal.OutletComponent;

@NgModule(declarations = { OutletComponent.class, RouterLinkDirective.class }, providers = { Router.class })
public class RouterModule {

	public static ModuleWithProviders<RouterModule> forRoot(Config config) {
		return ModuleWithProviders.<RouterModule>of(RouterModule.class)
				.providers(useValue(Config.class, config), config.getLocationProvider())
				.declarations(routeComponents(config))
				.build();
	}

	private static Class<?>[] routeComponents(Config config) {
		return config.getRoutes()
				.stream()
				.map(Route::getComponent)
				.collect(toList())
				.toArray(new Class<?>[0]);
	}
}
