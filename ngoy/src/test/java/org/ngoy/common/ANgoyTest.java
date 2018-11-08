package org.ngoy.common;

import static org.ngoy.Ngoy.app;
import static org.ngoy.core.NgoyException.wrap;

import java.io.ByteArrayOutputStream;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.Rule;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.ngoy.core.Provider;

public abstract class ANgoyTest {

	public static Path getTestPath() {
		return Paths.get(System.getProperty("user.dir"), "src\\test\\java\\org\\ngoy");
	}

	public static Path getTestResourcesPath() {
		return Paths.get(System.getProperty("user.dir"), "src\\test\\resources\\org\\ngoy");
	}

	protected boolean debugPrint;

	@Rule
	public MockitoRule mockitoRule = MockitoJUnit.rule();

	protected String render(Class<?> clazz, Provider... providers) {
		try {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			app(clazz).parseBody(true)
					.providers(providers)
					.build()
					.render(baos);
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
