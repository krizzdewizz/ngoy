package org.ngoy;

import static org.ngoy.core.Provider.useValue;

import java.util.Locale;

import javax.servlet.http.HttpServletResponse;

import org.ngoy.core.Context;
import org.ngoy.core.TemplateCache;
import org.ngoy.testapp.PersonService;
import org.ngoy.testapp.TestApp;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class GreetingViewController {

	private static Ngoy ngoy;

	@GetMapping(path = "/")
	@ResponseBody
	public void greeting(@RequestParam(name = "name", required = false, defaultValue = "world") String name, HttpServletResponse response) throws Exception {

		// simply render a template, bind to variables. No 'app' needed

		Context ctx = Context.of()
				.variable("name", name);
		Ngoy.render("/templates/greeting.html", ctx, response.getOutputStream());
	}

	@GetMapping(path = "/p")
	@ResponseBody
	public void persons(HttpServletResponse response) throws Exception {
		// do not disable in production
		TemplateCache.DEFAULT.setDisabled(true);

		app().render(response.getOutputStream());
	}

	private Ngoy app() {
		if (ngoy == null) {
			PersonService personService = new PersonService();
			ngoy = Ngoy.app(TestApp.class)
					.providers(useValue(PersonService.class, personService))
					.locale(Locale.GERMAN)
					.translateBundle("messages")
					.build();
		}
		return ngoy;
	}
}