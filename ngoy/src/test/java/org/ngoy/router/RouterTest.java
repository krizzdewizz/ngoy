package org.ngoy.router;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.ngoy.core.Provider.useValue;

import org.junit.Test;
import org.ngoy.ANgoyTest;
import org.ngoy.Modules;
import org.ngoy.core.Component;
import org.ngoy.core.ModuleWithProviders;

public class RouterTest extends ANgoyTest {

	@Component(selector = "home", template = "hello home<a routerLink=\"/settings\">goto settings</a>")
	public static class HomeCmp {
	}

	@Component(selector = "settings", template = "hello settings")
	public static class SettingsCmp {
	}

	@Component(selector = "test", template = "router test:<router-outlet></router-outlet>")
	public static class Cmp {
		public int activeRoute = 0;
	}

	@Test
	public void test() {
		Location location = mock(Location.class);
		when(location.getPath()).thenReturn("/app");

		Config routerConfig = Config //
				.baseHref("/app")
				.location(useValue(Location.class, location))
				.route("home", HomeCmp.class)
				.route("settings", SettingsCmp.class)
				.build();

		ModuleWithProviders<RouterModule> router = RouterModule.forRoot(routerConfig);
		Modules<RouterModule> routerModule = Modules.of(router);

		assertThat(render(Cmp.class, routerModule)).isEqualTo("router test:<router-outlet><home>hello home<a href=\"/settings\">goto settings</a></home></router-outlet>");

		when(location.getPath()).thenReturn("/app/settings");
		assertThat(render(Cmp.class, routerModule)).isEqualTo("router test:<router-outlet><settings>hello settings</settings></router-outlet>");
	}
}
