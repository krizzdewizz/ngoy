package ngoy.parser;

import static java.util.Arrays.asList;
import static java.util.Collections.emptySet;
import static ngoy.core.Util.copyToString;
import static ngoy.core.Util.newPrintStream;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.junit.Ignore;
import org.junit.Test;

import jodd.jerry.Jerry;
import ngoy.ANgoyTest;
import ngoy.JavaTemplate;
import ngoy.core.Directive;
import ngoy.core.Injector;
import ngoy.core.LocaleProvider;
import ngoy.core.Provider;
import ngoy.core.Util;
import ngoy.core.internal.CmpRef;
import ngoy.core.internal.Ctx;
import ngoy.core.internal.DefaultInjector;
import ngoy.core.internal.Resolver;
import ngoy.internal.parser.ByteCodeTemplate;
import ngoy.internal.parser.Parser;
import ngoy.model.Person;
import ngoy.testapp.PersonDetailComponent;
import ngoy.translate.TranslateDirective;
import ngoy.translate.TranslateService;

@Ignore
public class ParserTest {

	@Test
	public void parseToByteCode() throws Exception {
		Parser parser = new Parser();
		ByteCodeTemplate bb = new ByteCodeTemplate("ngoy.XByteCode", null);
		parser.parse(copyToString(getClass().getResourceAsStream("test.html")), bb);

		Class<?> clazz = bb.getClassFile()
				.defineClass();
		Ctx ctx = Ctx.of()
				.variable("x", true)
				.variable("person", new Person("krizz"))
				.variable("persons", asList(new Person("krizz"), new Person("qbert")));
		ctx.setOut(System.out, null);
		Method m = clazz.getMethod("render", Ctx.class);
		m.invoke(null, ctx);
	}

	@Test
	public void parseJavaToJava() throws Exception {
		DefaultInjector injector = new DefaultInjector(Provider.of(TranslateDirective.class), Provider.of(TranslateService.class),
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
				return null;
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
		});
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		PrintStream out = newPrintStream(baos);
		parser.parse(copyToString(getClass().getResourceAsStream("test.html")), new JavaTemplate(out));
		out.flush();
		out.close();
		// System.out.println(flatten(html));
		String html = new String(baos.toByteArray(), "UTF-8");

		Path src = ANgoyTest.getTestPath()
				.resolve("X.java");
		Files.write(src, html.getBytes("UTF-8"));

		// assertThat(flatten(html)).isEqualTo(
		// "<body> <div class=\"x y\"> abc hello krizz <br> hello <a href=\"x\"
		// title=\"gogo\">you</a> z </div> </body>");
	}

	String flatten(String html) {
		return html.replaceAll("\\n", "");
	}

}
