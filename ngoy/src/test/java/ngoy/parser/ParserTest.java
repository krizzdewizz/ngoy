package ngoy.parser;

import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;
import static java.util.Collections.emptySet;
import static ngoy.core.Util.copyToString;
import static ngoy.core.Util.newPrintStream;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import jodd.jerry.Jerry;
import ngoy.common.UpperCasePipe;
import ngoy.core.Directive;
import ngoy.core.Injector;
import ngoy.core.LocaleProvider;
import ngoy.core.Provider;
import ngoy.core.Util;
import ngoy.core.internal.CmpRef;
import ngoy.core.internal.DefaultInjector;
import ngoy.core.internal.Resolver;
import ngoy.internal.parser.Parser;
import ngoy.internal.parser.template.JavaTemplate;
import ngoy.testapp.PersonDetailComponent;
import ngoy.translate.TranslateDirective;
import ngoy.translate.TranslateService;

public class ParserTest {

//	@org.junit.Test
	public void parseJavaToJava() throws Exception {

		DefaultInjector injector = new DefaultInjector(Provider.of(PersonDetailComponent.class), Provider.of(TranslateDirective.class), Provider.of(TranslateService.class),
				Provider.useValue(LocaleProvider.class, new LocaleProvider.Default(Locale.ENGLISH)));
		Parser parser = new Parser(new Resolver() {
			@Override
			public List<CmpRef> resolveCmps(Jerry el) {

				List<CmpRef> all = new ArrayList<>();

				if (el.is(TranslateDirective.class.getAnnotation(Directive.class)
						.selector())) {
					all.add(new CmpRef(TranslateDirective.class, "", true));
				}

				String nodeName = el.get(0)
						.getNodeName();

				if (nodeName.equals("person")) {
					all.add(new CmpRef(PersonDetailComponent.class, Util.getTemplate(PersonDetailComponent.class), false));
				}

				return all;
			}

			@Override
			public Class<?> resolvePipe(String name) {
				return "uppercase".equals(name) ? UpperCasePipe.class : null;
			}

			@Override
			public Injector getInjector() {
				return injector;
			}

			@Override
			public Class<?> resolveCmpClass(Class<?> cmpClass) {
				return null;
			}

			@Override
			public Set<Class<?>> getCmpClasses() {
				return emptySet();
			}

			@Override
			public Class<?> getAppRoot() {
				return null;
			}

			@Override
			public List<Class<?>> resolvePipes() {
				return emptyList();
			}
		});
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		PrintStream out = newPrintStream(baos);
		parser.parse(copyToString(getClass().getResourceAsStream("test.html")), new JavaTemplate(out, "", false, emptyMap()));
		out.flush();
		out.close();
		// System.out.println(flatten(html));
		String html = new String(baos.toByteArray(), "UTF-8");

		Path src = getTestPath().resolve("X.java");
		Files.write(src, html.getBytes("UTF-8"));
	}

	String flatten(String html) {
		return html.replaceAll("\\n", "");
	}

	static Path getTestPath() {
		return Paths.get(System.getProperty("user.dir"), "src/test/java/ngoy");
	}

}
