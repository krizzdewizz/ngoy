package org.ngoy;

import static org.ngoy.Ngoy.app;
import static org.ngoy.core.NgoyException.wrap;

import java.io.ByteArrayOutputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.function.Function;

import org.junit.Rule;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.ngoy.Ngoy.Builder;
import org.ngoy.core.Provider;

public abstract class ANgoyTest {

	public static Path getTestPath() {
		return Paths.get(System.getProperty("user.dir"), "src\\test\\java\\org\\ngoy");
	}

	public static Path getTestResourcesPath() {
		return Paths.get(System.getProperty("user.dir"), "src\\test\\resources\\org\\ngoy");
	}

	protected boolean debugPrint;
	protected Boolean parseForJUnit;

	@Rule
	public MockitoRule mockitoRule = MockitoJUnit.rule();

	protected String render(Class<?> clazz, Provider... providers) {
		return render(clazz, Objects::requireNonNull, providers);
	}

	protected String render(Class<?> clazz, Function<Ngoy.Builder, Ngoy.Builder> onBuild, Provider... providers) {
		try {

			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			Builder builder = app(clazz).parseBody(true)
					.providers(providers);
			Ngoy app = onBuild.apply(builder)
					.build();
			app.render(baos);
			app.destroy();
			String html = new String(baos.toByteArray(), "UTF-8");
			if (debugPrint) {
				System.out.println(html);
			}
			return html;
		} catch (Exception e) {
			throw wrap(e);
		}
	}
}
