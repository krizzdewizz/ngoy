package ngoy;

import static ngoy.Ngoy.app;
import static ngoy.core.NgoyException.wrap;

import java.io.ByteArrayOutputStream;
import java.util.Objects;
import java.util.function.Function;

import org.junit.Rule;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import ngoy.Ngoy.Builder;
import ngoy.core.Provider;

public abstract class ANgoyTest {

	protected boolean debugPrint;

	@Rule
	public MockitoRule mockitoRule = MockitoJUnit.rule();

	protected String render(Class<?> clazz, Provider... providers) {
		return render(clazz, Objects::requireNonNull, providers);
	}

	protected String render(Class<?> clazz, Function<Ngoy.Builder<?>, Ngoy.Builder<?>> onBuild, Provider... providers) {
		try {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			Builder<?> builder = app(clazz).providers(providers);
			Ngoy<?> app = onBuild.apply(builder)
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
