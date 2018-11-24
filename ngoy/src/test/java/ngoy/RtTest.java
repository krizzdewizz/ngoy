package ngoy;

import static java.lang.String.format;
import static ngoy.ANgoyTest.getTestPath;
import static ngoy.core.NgoyException.wrap;
import static ngoy.core.Util.newPrintStream;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;

import ngoy.Ngoy.Config;
import ngoy.core.Injector;
import ngoy.core.ModuleWithProviders;
import ngoy.core.Provider;
import ngoy.core.Util;
import ngoy.core.internal.Ctx;
import ngoy.internal.parser.Parser;
import ngoy.testapp.PersonService;
import ngoy.testapp.TestApp;

public class RtTest {

//	@org.junit.Test
	public void run() {
		Ngoy<TestApp> rt = new Ngoy<TestApp>(TestApp.class, new Config(), new Injector[0], new ModuleWithProviders[0], Provider.of(PersonService.class)) {
			@Override
			protected void parseAndRender(Class<TestApp> appRoot, String template, Parser parser, Ctx ctx, PrintStream out) {
				try {
					ByteArrayOutputStream baos = new ByteArrayOutputStream();
					parser.parse(Util.getTemplate(appRoot), new JavaTemplate(newPrintStream(out)));

					String html = new String(baos.toByteArray(), "UTF-8");
					Path src = getTestPath().resolve("X.java");
					System.out.println(format("see %s", src));
					Files.write(src, html.getBytes("UTF-8"));
				} catch (Exception e) {
					throw wrap(e);
				}
			}
		};

		rt.render(System.out);
	}
}
