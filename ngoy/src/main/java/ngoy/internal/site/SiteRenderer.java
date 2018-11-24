package ngoy.internal.site;

import static java.lang.String.format;

import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import ngoy.Ngoy;
import ngoy.core.Inject;
import ngoy.core.NgoyException;
import ngoy.core.Optional;
import ngoy.core.internal.StyleUrlsDirective;
import ngoy.router.Location;
import ngoy.router.Route;
import ngoy.router.Router;

public class SiteRenderer {

	@Inject
	@Optional
	public Router router;

	@Inject
	public StyleUrlsDirective styleUrlsDirective;

	public void render(Ngoy<?> ngoy, Path folder) {
		if (router != null) {
			renderRoutes(ngoy, folder);
			writeCss(folder);
		} else {
			render(ngoy, folder, "index.html");
		}
	}

	private void writeCss(Path folder) {
		try {
			String styles = styleUrlsDirective.getStyles();
			if (styles.isEmpty()) {
				return;
			}
			Path stylesFolder = folder.resolve("styles");
			Files.createDirectories(stylesFolder);
			Files.write(stylesFolder.resolve("main.css"), styles.getBytes("UTF-8"));
		} catch (Exception e) {
			throw NgoyException.wrap(e);
		}
	}

	private void renderRoutes(Ngoy<?> ngoy, Path folder) {
		Location oldLocation = router.location;
		styleUrlsDirective.href = "styles/main.css";
		try {
			for (Route route : router.getRoutes()) {
				String path = route.getPath();

				String baseHref = router.config.getBaseHref();
				String slash = "/".equals(baseHref) ? "" : "/";
				String location = format("%s%s%s", baseHref, slash, path);
				router.location = () -> location;

				String file = format("%s.html", path);
				render(ngoy, folder, file);
			}
		} finally {
			router.location = oldLocation;
			styleUrlsDirective.href = null;
		}
	}

	private void render(Ngoy<?> ngoy, Path folder, String file) {
		Path page = folder.resolve(file);
		try {
			Files.createDirectories(page.getParent());

			try (OutputStream out = Files.newOutputStream(page)) {
				ngoy.render(out);
			}
		} catch (Exception e) {
			throw new NgoyException(e, "Error while rendering site page '%s'", page);
		}
	}
}
