package ngoy;

import static ngoy.core.Provider.useValue;

import java.util.Locale;

import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import ngoy.core.Context;
import ngoy.core.TemplateCache;
import ngoy.testapp.PersonService;
import ngoy.testapp.TestApp;

@Controller
public class GreetingViewController implements InitializingBean {

	private Ngoy<TestApp> ngoy;

	@GetMapping(path = "/")
	public void greeting(@RequestParam(name = "name", required = false, defaultValue = "world") String name, HttpServletResponse response) throws Exception {

		// simply render a template, bind to variables. No 'app' needed

		Context ctx = Context.of()
				.variable("name", name);
		Ngoy.renderTemplate("/templates/greeting.html", ctx, response.getOutputStream());
	}

	@GetMapping(path = "/more")
	public void persons(HttpServletResponse response) throws Exception {
		// do not disable in production
		TemplateCache.DEFAULT.setDisabled(true);

		ngoy.render(response.getOutputStream());
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		PersonService personService = new PersonService();
		ngoy = Ngoy.app(TestApp.class)
				.providers(useValue(PersonService.class, personService))
				.locale(Locale.GERMAN)
				.translateBundle("ngoy.testapp.messages")
				.build();

		ngoy.getAppInstance()
				.toString();
	}
}