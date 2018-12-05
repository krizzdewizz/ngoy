package ngoy;

import static java.util.Collections.emptyMap;
import static ngoy.core.Util.newPrintStream;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.nio.file.Path;
import java.nio.file.Paths;

import ngoy.common.DatePipe;
import ngoy.common.LowerCasePipe;
import ngoy.common.UpperCasePipe;
import ngoy.core.Provider;
import ngoy.core.internal.Ctx;
import ngoy.core.internal.DefaultInjector;
import ngoy.model.Person;
import ngoy.testapp.PersonDetailComponent;
import ngoy.testapp.PersonService;
import ngoy.testapp.TestApp;

public class XTest {

	private static Path getTestResourcesPath() {
		return Paths.get(System.getProperty("user.dir"), "src\\test\\resources\\ngoy");
	}

//	@org.junit.Test
	public void testNgoy() throws Exception {
		Ngoy<TestApp> ng = Ngoy.app(TestApp.class)
				.providers(Provider.of(PersonService.class))
				.build();
		ng.render(System.out);
	}

//	@org.junit.Test
	public void test() throws Exception {
		TestApp testApp = new TestApp();
		testApp.personService = new PersonService();
		Ctx ctx = Ctx.of(testApp, new DefaultInjector( //
				Provider.of(PersonDetailComponent.class), //
				Provider.of(DatePipe.class), //
				Provider.of(UpperCasePipe.class), //
				Provider.of(LowerCasePipe.class)), //
				emptyMap() //
		)
				.variable("person", new Person("krizz"))//
				.variable("x", true);
		File target = getTestResourcesPath().resolve("x.html")
				.toFile();
		try (PrintStream out = newPrintStream(new FileOutputStream(target))) {
			ctx.setOut(out, null);
//			X.render(ctx);
		} finally {
			ctx.resetOut();
		}
	}
}
