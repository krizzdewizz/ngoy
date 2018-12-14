package ngoy;

import ngoy.core.Provider;
import ngoy.testapp.PersonService;
import ngoy.testapp.TestApp;

public class XTest {
//
//	private static Path getTestResourcesPath() {
//		return Paths.get(System.getProperty("user.dir"), "src\\test\\resources\\ngoy");
//	}

//	@org.junit.Test
	public void testNgoy() throws Exception {
		Ngoy<TestApp> ng = Ngoy.app(TestApp.class)
				.providers(Provider.of(PersonService.class))
				.build();
		ng.render(System.out);
	}
}
