package org.ngoy.parser;

import static java.util.Arrays.asList;
import static java.util.Collections.emptySet;
import static org.ngoy.core.Util.copyToString;
import static org.ngoy.core.Util.newPrintStream;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.jsoup.nodes.Element;
import org.junit.Test;
import org.ngoy.ANgoyTest;
import org.ngoy.JavaTemplate;
import org.ngoy.Ngoy;
import org.ngoy.core.Directive;
import org.ngoy.core.Injector;
import org.ngoy.core.LocaleProvider;
import org.ngoy.core.Provider;
import org.ngoy.core.internal.CmpRef;
import org.ngoy.core.internal.Ctx;
import org.ngoy.core.internal.DefaultInjector;
import org.ngoy.core.internal.Resolver;
import org.ngoy.internal.parser.ByteCodeTemplate;
import org.ngoy.internal.parser.Parser;
import org.ngoy.model.Person;
import org.ngoy.testapp.PersonDetailComponent;
import org.ngoy.translate.TranslateDirective;
import org.ngoy.translate.TranslateService;

public class ParserTest {

	@Test
	public void parseToByteCode() throws Exception {
		Parser parser = new Parser();
		ByteCodeTemplate bb = new ByteCodeTemplate("org.ngoy.XByteCode", null);
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
			public List<CmpRef> resolveCmps(Element el) {

				List<CmpRef> all = new ArrayList<>();

				if (el.is(TranslateDirective.class.getAnnotation(Directive.class)
						.selector())) {
					all.add(new CmpRef(TranslateDirective.class, "", true, ""));
				}

				String nodeName = el.nodeName();

				if (nodeName.equals("person")) {
					all.add(new CmpRef(PersonDetailComponent.class, Ngoy.getTemplate(PersonDetailComponent.class), false, ""));
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
			public String resolveCmpClass(String cmpClass) {
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
