package ngoy.site;

import static ngoy.core.Provider.useValue;
import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import ngoy.ANgoyTest;
import ngoy.Ngoy;
import ngoy.core.Component;
import ngoy.core.NgModule;
import ngoy.router.Location;
import ngoy.router.RouterConfig;
import ngoy.router.RouterModule;
import ngoy.router.RouterTest.ACmp;

public class SiteRendererTest extends ANgoyTest {

	@Rule
	public TemporaryFolder folder = new TemporaryFolder();

	@Component(selector = "home", template = "hello home")
	public static class HomeCmp {
	}

	@Component(selector = "settings", template = "hello settings")
	public static class SettingsCmp {
	}

	@Component(selector = "test", template = "site:<router-outlet></router-outlet>")
	@NgModule(declarations = { ACmp.class })
	public static class Site {
	}

	@Test
	public void test() throws Exception {
		RouterConfig routerConfig = RouterConfig //
				.baseHref("/app")
				.location(useValue(Location.class, () -> "")) // not used for site renderer
				.route("index", HomeCmp.class)
				.route("abc/settings", SettingsCmp.class)
				.build();

		Ngoy ngoy = Ngoy.app(Site.class)
				.modules(RouterModule.forRoot(routerConfig))
				.build();

		Path f = folder.newFolder()
				.toPath();
		ngoy.renderSite(f);

		String index = readFile(f.resolve("index.html"));
		assertThat(index).isEqualTo("site:<router-outlet><home>hello home</home></router-outlet>");

		String settings = readFile(f.resolve("abc/settings.html"));
		assertThat(settings).isEqualTo("site:<router-outlet><settings>hello settings</settings></router-outlet>");
	}

	private String readFile(Path file) throws UnsupportedEncodingException, IOException {
		return new String(Files.readAllBytes(file), "UTF-8");
	}
}
