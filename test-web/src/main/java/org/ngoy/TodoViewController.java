package org.ngoy;

import static org.ngoy.todo.TodoEvent.ADD_TODO;
import static org.ngoy.todo.TodoEvent.DELETE_TODO;

import javax.servlet.http.HttpServletResponse;

import org.ngoy.core.TemplateCache;
import org.ngoy.todo.AppComponent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class TodoViewController {

	private static Ngoy ngoy;

	@Autowired
	private BeanInjector beanInjector;

	@GetMapping(path = "/todo")
	public void todos(HttpServletResponse response) throws Exception {
		// do not disable in production
		TemplateCache.DEFAULT.setDisabled(true);

		app().render(response.getOutputStream());
	}

	@PostMapping("/todo")
	public String addTodo(@RequestParam("text") String text) throws Exception {
		app().publish(ADD_TODO, text);
		return "redirect:todo";
	}

	@PostMapping("/tododelete")
	public String todoDelete(@RequestParam("id") String id) throws Exception {
		app().publish(DELETE_TODO, id);
		return "redirect:todo";
	}

	private Ngoy app() {
		if (ngoy == null) {
			ngoy = Ngoy.app(AppComponent.class)
					.injectors(beanInjector)
					.build();
		}
		return ngoy;
	}
}