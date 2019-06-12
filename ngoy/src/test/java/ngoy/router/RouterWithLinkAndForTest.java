package ngoy.router;

import ngoy.ANgoyTest;
import ngoy.Modules;
import ngoy.core.Component;
import ngoy.core.Inject;
import ngoy.core.Input;
import ngoy.core.ModuleWithProviders;
import ngoy.core.NgModule;
import org.junit.Test;

import static ngoy.core.Provider.useValue;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class RouterWithLinkAndForTest extends ANgoyTest {

	@Component(selector = "x", template = "XX")
	public static class XCmp {
		@Input
		public Object route;

	}

	@Component(selector = "home", template = "hello home")
	public static class HomeCmp {
	}

	@Component(selector = "settings", template = "hello settings")
	public static class SettingsCmp {
	}

	@Component(selector = "test", template = "<a [routerLink]=\"'/x'\"></a>router test:<a *ngFor=\"let route of config.routes\" [routerLink]=\"'/' + route.path\"><x [route]=\"route\"></x></a>")
	@NgModule(declarations = { XCmp.class })
	public static class Cmp {
		@Inject
		public RouterConfig config;
	}

	@Test
	public void test() {
		Location location = mock(Location.class);
		when(location.getPath()).thenReturn("/app");

		RouterConfig routerConfig = RouterConfig //
				.baseHref("/app")
				.location(useValue(Location.class, location))
				.route("home", HomeCmp.class)
				.route("settings", SettingsCmp.class)
				.build();

		ModuleWithProviders<RouterModule> router = RouterModule.forRoot(routerConfig);
		Modules<RouterModule> routerModule = Modules.of(router);

		assertThat(render(Cmp.class, routerModule)).isEqualTo("<a href=\"/x\"></a>router test:<a href=\"/home\"><x>XX</x></a><a href=\"/settings\"><x>XX</x></a>");
	}
}
