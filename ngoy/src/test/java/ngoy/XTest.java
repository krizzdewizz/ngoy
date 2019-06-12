package ngoy;

import ngoy.core.Provider;
import ngoy.testapp.PersonService;
import ngoy.testapp.TestApp;

import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;

public class XTest {
//	@org.junit.Test
	public void testNgoy() throws Exception {
		Ngoy<TestApp> ng = Ngoy.app(TestApp.class)
				.providers(Provider.of(PersonService.class))
				.build();
		Writer out = new OutputStreamWriter(System.out, StandardCharsets.UTF_8);
		ng.render(out);
		out.close();
	}
}
