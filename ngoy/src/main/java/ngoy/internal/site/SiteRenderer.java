package ngoy.internal.site;

import ngoy.Ngoy;
import ngoy.core.Inject;
import ngoy.core.NgoyException;
import ngoy.core.Optional;
import ngoy.core.internal.StyleUrlsDirective;
import ngoy.router.Router;

import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.function.Function;

import static java.lang.String.format;
import static ngoy.core.NgoyException.wrap;

public class SiteRenderer {

    private static final String MAIN_CSS = "styles/main.css";

    private static String defaultLinkRenderer(String routerLink) {
        return routerLink;
    }

    private static String staticLinkRenderer(String routerLink) {
        if (routerLink.startsWith("/")) {
            routerLink = routerLink.substring(1);
        }
        return format("%s.html", routerLink.replace('/', '_'));
    }

    @Inject
    @Optional
    public Router router;

    @Inject
    public StyleUrlsDirective styleUrlsDirective;

    private Function<String, String> linkRenderer = SiteRenderer::defaultLinkRenderer;

    public void render(Ngoy<?> ngoy, Path folder, List<String> routes, Runnable compile) {
        try {
            linkRenderer = SiteRenderer::staticLinkRenderer;
            if (router != null) {
                renderRoutes(ngoy, folder, routes, compile);
                writeCss(folder);
            } else {
                renderPage(ngoy, folder.resolve("index.html"));
            }
        } finally {
            linkRenderer = SiteRenderer::defaultLinkRenderer;
        }
    }

    private void renderRoutes(Ngoy<?> ngoy, Path folder, List<String> routes, Runnable compile) {
        styleUrlsDirective.config.href = MAIN_CSS;
        try {
            compile.run();
            router.withRoutesDo(routes, path -> {
                String baseHref = router.config.getBaseHref();
                if (path.startsWith(baseHref)) {
                    path = path.substring(baseHref.length());
                }
                if (path.startsWith("/")) {
                    path = path.substring(1);
                }
                String file = toStaticLink(path);
                renderPage(ngoy, folder.resolve(file));
            });
        } finally {
            styleUrlsDirective.config.href = null;
        }
    }

    private void renderPage(Ngoy<?> ngoy, Path page) {
        try {
            ensureDirectory(page);
            try (Writer out = Files.newBufferedWriter(page, StandardCharsets.UTF_8)) {
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
            Files.write(cssFile, styles.getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            throw wrap(e);
        }
    }

    private void ensureDirectory(Path file) {
        try {
            Files.createDirectories(file.getParent());
        } catch (Exception e) {
            throw wrap(e);
        }
    }

    public String toStaticLink(String routerLink) {
        return linkRenderer.apply(routerLink);
    }
}
