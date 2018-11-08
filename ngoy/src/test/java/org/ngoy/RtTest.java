package org.ngoy;

import static java.lang.String.format;
import static org.ngoy.common.ANgoyTest.getTestPath;
import static org.ngoy.core.NgoyException.wrap;
import static org.ngoy.internal.util.Util.newPrintStream;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.Test;
import org.ngoy.Ngoy.Config;
import org.ngoy.core.Provider;
import org.ngoy.core.internal.Ctx;
import org.ngoy.internal.parser.Parser;
import org.ngoy.testapp.PersonService;
import org.ngoy.testapp.TestApp;

public class RtTest {

	@Test
	public void run() {
		Ngoy rt = new Ngoy(TestApp.class, new Config(), null, Provider.of(PersonService.class)) {
			@Override
			protected void parseAndRender(Class<?> appRoot, Parser parser, Ctx ctx, PrintStream out) {
				try {
					ByteArrayOutputStream baos = new ByteArrayOutputStream();
					parser.parse(Ngoy.getTemplate(appRoot), new JavaTemplate(newPrintStream(out)));

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
