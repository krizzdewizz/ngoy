package ngoy.router;

import ngoy.core.ModuleWithProviders;
import ngoy.core.NgModule;
import ngoy.core.Provide;
import ngoy.router.internal.OutletComponent;

import static ngoy.core.Provider.useValue;

@NgModule(declarations = {OutletComponent.class, RouterLinkDirective.class}, providers = {Router.class, RouteParams.class}, provide = {
        @Provide(provide = ActiveRouteProvider.class, useClass = DefaultActiveRouteProvider.class)})
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
