package org.ngoy;

import javax.servlet.http.HttpServletResponse;

import org.ngoy.core.TemplateCache;
import org.ngoy.todo.AppComponent;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class TodoViewController {

	private static Ngoy ngoy;

	@GetMapping(path = "/todo")
	public void persons(HttpServletResponse response) throws Exception {
		// do not disable in production
		TemplateCache.DEFAULT.setDisabled(true);

		app().render(response.getOutputStream());
	}

	@PostMapping("/todo")
	public void todoSubmit(@RequestParam("text") String text, HttpServletResponse response) throws Exception {
		app().post("todo.add", text)
				.render(response.getOutputStream());
	}

	@PostMapping("/todo/delete")
	public void todoDelete(@RequestParam("id") String id, HttpServletResponse response) throws Exception {
		app().post("todo.delete", id)
				.render(response.getOutputStream());
	}

	private Ngoy app() {
		if (ngoy == null || "a".isEmpty()) {
			ngoy = Ngoy.app(AppComponent.class)
					.build();
		}
		return ngoy;
	}
}