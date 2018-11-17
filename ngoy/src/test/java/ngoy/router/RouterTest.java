package ngoy.router;

import static ngoy.core.Provider.useValue;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Test;

import ngoy.ANgoyTest;
import ngoy.Modules;
import ngoy.core.Component;
import ngoy.core.ModuleWithProviders;
import ngoy.core.NgModule;
import ngoy.router.Location;
import ngoy.router.RouterConfig;
import ngoy.router.RouterModule;

public class RouterTest extends ANgoyTest {

	@Component(selector = "a", template = "<x>x<ng-content></ng-content></x>")
	public static class ACmp {
	}

	@Component(selector = "home", template = "hello home<a [class.active]=\"ok\" [routerLink]=\"route\">goto settings</a>")
	public static class HomeCmp {
		public boolean ok = true;
		public String route = "/settings";
	}

	@Component(selector = "settings", template = "hello settings")
	public static class SettingsCmp {
	}

	@Component(selector = "test", template = "router test:<router-outlet></router-outlet>")
	@NgModule(declarations = { ACmp.class })
	public static class Cmp {
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

		assertThat(render(Cmp.class, routerModule)).isEqualTo("router test:<router-outlet><home>hello home<a class=\"active\" href=\"/settings\">xgoto settings</a></home></router-outlet>");

		when(location.getPath()).thenReturn("/app/settings");
		assertThat(render(Cmp.class, routerModule)).isEqualTo("router test:<router-outlet><settings>hello settings</settings></router-outlet>");
	}
}
