package org.ngoy;

import static org.ngoy.common.ANgoyTest.getJngTestResourcesPath;
import static org.ngoy.internal.util.Util.newPrintStream;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintStream;

import org.junit.Test;
import org.ngoy.Ngoy;
import org.ngoy.common.DatePipe;
import org.ngoy.common.LowerCasePipe;
import org.ngoy.common.UpperCasePipe;
import org.ngoy.core.Provider;
import org.ngoy.core.internal.Ctx;
import org.ngoy.core.internal.Injector;
import org.ngoy.model.Person;
import org.ngoy.testapp.PersonDetailComponent;
import org.ngoy.testapp.PersonService;
import org.ngoy.testapp.TestApp;

public class XTest {

	@Test
	public void testJng() throws Exception {
		Ngoy ng = Ngoy.app(TestApp.class)
				.providers(Provider.of(PersonService.class))
				.build();
		ng.render(System.out);
	}

	@Test
	public void test() throws Exception {
		TestApp testApp = new TestApp();
		testApp.personService = new PersonService();
		Ctx ctx = Ctx.of(testApp, new Injector( //
				Provider.of(PersonDetailComponent.class), //
				Provider.of(DatePipe.class), //
				Provider.of(UpperCasePipe.class), //
				Provider.of(LowerCasePipe.class))//
		)
				.variable("person", new Person("krizz"))//
				.variable("x", true);
		File target = getJngTestResourcesPath().resolve("x.html")
				.toFile();
		try (PrintStream out = newPrintStream(new FileOutputStream(target))) {
			ctx.setOut(out, null);
			X.render(ctx);
		} finally {
			ctx.resetOut();
		}
	}
}
