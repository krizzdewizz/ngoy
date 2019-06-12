package ngoy.router;

import ngoy.ANgoyTest;
import ngoy.Modules;
import ngoy.core.Component;
import ngoy.core.Inject;
import ngoy.core.ModuleWithProviders;
import ngoy.core.NgModule;
import ngoy.core.Provider;
import org.junit.Test;

import static ngoy.core.Provider.useValue;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class RouterTest extends ANgoyTest {

	@Component(selector = "a", template = "<x>x<ng-content></ng-content></x>")
	public static class ACmp {
	}

	@Component(selector = "home", template = "hello home<a [class.active]=\"ok\" [routerLink]=\"route\">goto settings</a>")
	public static class HomeCmp {
		public boolean ok = true;
		public String route = "/settings";
	}

	@Component(selector = "settings", template = "hello settings. params: {{ params.getOrNull('query') }}")
	public static class SettingsCmp {
		@Inject
		public RouteParams params;
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

		assertThat(render(Cmp.class, routerModule)).isEqualTo("router test:<home>hello home<a class=\"active\" href=\"/settings\"><x>xgoto settings</x></a></home>");

		when(location.getPath()).thenReturn("/app/settings");
		assertThat(render(Cmp.class, routerModule)).isEqualTo("router test:<settings>hello settings. params: </settings>");
	}

	public static class TestRouteProvider implements ActiveRouteProvider {

		@Override
		public ActiveRoute getActiveRoute(String path) {
			String prefix = "/app/settings";
			if (path.startsWith(prefix)) {
				String right = path.substring(prefix.length());
				int pos = right.indexOf('?');
				RouteParams params = new RouteParams();
				if (pos >= 0) {
					String param = right.substring(pos + 1);
					params.put("query", param);
				}
				return new ActiveRoute(1, params);
			}
			return null;
		}
	}

	@Test
	public void testActiveRouteProvider() {
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

		Provider routeProvider = Provider.useClass(ActiveRouteProvider.class, TestRouteProvider.class);

		assertThat(render(Cmp.class, routerModule, routeProvider)).isEqualTo("router test:<home>hello home<a class=\"active\" href=\"/settings\"><x>xgoto settings</x></a></home>");

		when(location.getPath()).thenReturn("/app/settings?abc");
		assertThat(render(Cmp.class, routerModule, routeProvider)).isEqualTo("router test:<settings>hello settings. params: abc</settings>");
	}
}
