package org.ngoy.router;

import static java.util.stream.Collectors.toList;
import static org.ngoy.core.Provider.useValue;

import java.util.List;

import org.ngoy.core.ModuleWithProviders;
import org.ngoy.core.NgModule;
import org.ngoy.router.outlet.OutletComponent;

@NgModule(declarations = { OutletComponent.class }, providers = { RouterService.class })
public class RouterModule {
	public static ModuleWithProviders<RouterModule> forRoot(Config config) {
		List<Class<?>> declarations = config.getRoutes()
				.stream()
				.map(Route::getComponent)
				.collect(toList());
		return ModuleWithProviders.of(RouterModule.class, declarations, useValue(Config.class, config), config.getLocationProvider());
	}
}
