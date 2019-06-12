package ngoy;

import ngoy.Ngoy.Builder;
import ngoy.core.Provider;
import org.junit.Rule;
import org.junit.rules.ExpectedException;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import java.io.StringWriter;
import java.util.Objects;
import java.util.function.Function;

import static ngoy.Ngoy.app;
import static ngoy.core.NgoyException.wrap;

public abstract class ANgoyTest {

	protected boolean debugPrint;

	@Rule
	public MockitoRule mockitoRule = MockitoJUnit.rule();

	@Rule
	public ExpectedException expectedEx = ExpectedException.none();

	protected String render(Class<?> clazz, Provider... providers) {
		return render(clazz, Objects::requireNonNull, providers);
	}

	protected String render(Class<?> clazz, Function<Ngoy.Builder<?>, Ngoy.Builder<?>> onBuild, Provider... providers) {
		try {
			StringWriter baos = new StringWriter();
			Builder<?> builder = app(clazz).providers(providers);
			Ngoy<?> app = onBuild.apply(builder)
					.build();
			app.render(baos);
			app.destroy();
			String html = baos.toString();
			if (debugPrint) {
				System.out.println(html);
			}
			return html;
		} catch (Exception e) {
			throw wrap(e);
		}
	}
}
