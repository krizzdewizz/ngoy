package ngoy.site;

import static java.lang.String.format;

import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import ngoy.Ngoy;
import ngoy.core.Inject;
import ngoy.core.NgoyException;
import ngoy.core.Optional;
import ngoy.router.Location;
import ngoy.router.Route;
import ngoy.router.Router;

public class SiteRenderer {

	@Inject
	@Optional
	public Router router;

	public void render(Ngoy ngoy, Path folder) {
		if (router != null) {
			renderRoutes(ngoy, folder);
		} else {
			render(ngoy, folder, "index.html");
		}
	}

	private void renderRoutes(Ngoy ngoy, Path folder) {
		Location oldLocation = router.location;
		try {
			for (Route route : router.getRoutes()) {
				String path = route.getPath();

				String location = format("%s/%s", router.config.getBaseHref(), path);
				router.location = () -> location;

				String file = format("%s.html", path);
				render(ngoy, folder, file);
			}
		} finally {
			router.location = oldLocation;
		}
	}

	private void render(Ngoy ngoy, Path folder, String file) {
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
