package ngoy;

import static ngoy.Ngoy.app;
import static ngoy.core.NgoyException.wrap;

import java.io.ByteArrayOutputStream;
import java.lang.annotation.Annotation;
import java.util.Objects;
import java.util.function.Function;

import org.junit.Rule;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import net.bytebuddy.ByteBuddy;
import ngoy.Ngoy.Builder;
import ngoy.core.Component;
import ngoy.core.Provide;
import ngoy.core.Provider;

public abstract class ANgoyTest {

	protected boolean debugPrint;

	@Rule
	public MockitoRule mockitoRule = MockitoJUnit.rule();

	protected String render(String template, Provider... providers) {
		return render(defineCmp(template), providers);
	}

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

	public static Class<?> defineCmp(String template) {
		return defineCmp(template, "");
	}

	public static Class<?> defineCmp(String template, String selector) {
		return new ByteBuddy().subclass(Object.class)
				.annotateType(new Component() {

					@Override
					public Class<? extends Annotation> annotationType() {
						return Component.class;
					}

					@Override
					public String selector() {
						return selector;
					}

					@Override
					public String template() {
						return template;
					}

					//

					@Override
					public String templateUrl() {
						return "";
					}

					@Override
					public String[] styleUrls() {
						return new String[0];
					}

					@Override
					public Class<?>[] providers() {
						return new Class[0];
					}

					@Override
					public Provide[] provide() {
						return new Provide[0];
					}

					@Override
					public String contentType() {
						return "";
					}

					@Override
					public String[] styles() {
						return new String[0];
					}
				})
				.make()
				.load(ANgoyTest.class.getClassLoader())
				.getLoaded();
	}
}
