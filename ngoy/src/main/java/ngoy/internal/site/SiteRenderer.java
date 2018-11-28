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
import ngoy.router.Router;

public class SiteRenderer {

	private static final String MAIN_CSS = "styles/main.css";

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
			render(ngoy, folder.resolve("index.html"));
		}
	}

	private void renderRoutes(Ngoy<?> ngoy, Path folder) {
		styleUrlsDirective.href = MAIN_CSS;
		try {
			router.withActivatedRoutesDo(route -> {
				String file = format("%s.html", route.getPath());
				renderPage(ngoy, folder.resolve(file));
			});
		} finally {
			styleUrlsDirective.href = null;
		}
	}

	private void renderPage(Ngoy<?> ngoy, Path page) {
		try {
			ensureDirectory(page);
			try (OutputStream out = Files.newOutputStream(page)) {
				ngoy.render(out);
			}
		} catch (Exception e) {
			throw new NgoyException(e, "Error while rendering site page '%s'", page);
		}
	}

	private void writeCss(Path folder) {
		try {
			String styles = styleUrlsDirective.getStyles();
			if (styles.isEmpty()) {
				return;
			}
			Path cssFile = folder.resolve(MAIN_CSS);
			ensureDirectory(cssFile);
			Files.write(cssFile, styles.getBytes("UTF-8"));
		} catch (Exception e) {
			throw NgoyException.wrap(e);
		}
	}

	private void ensureDirectory(Path file) {
		try {
			Files.createDirectories(file.getParent());
		} catch (Exception e) {
			throw NgoyException.wrap(e);
		}
	}
}
