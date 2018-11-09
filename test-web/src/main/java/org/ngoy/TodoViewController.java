package org.ngoy;

import static org.ngoy.core.Provider.useValue;
import static org.ngoy.todo.TodoEvent.TODO_DELETED;

import javax.servlet.http.HttpServletResponse;

import org.ngoy.core.TemplateCache;
import org.ngoy.todo.AppComponent;
import org.ngoy.todo.services.TodoService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class TodoViewController {

	private static Ngoy ngoy;

	private TodoService todoService = new TodoService();

	@GetMapping(path = "/todo")
	public void todos(HttpServletResponse response) throws Exception {
		// do not disable in production
		TemplateCache.DEFAULT.setDisabled(true);

		app().render(response.getOutputStream());
	}

	@PostMapping("/todo")
	public String addTodo(@RequestParam("text") String text, HttpServletResponse response) throws Exception {
		todoService.addTodo(text);
		return "redirect:todo";
	}

	@PostMapping("/tododelete")
	public String todoDelete(@RequestParam("id") String id, HttpServletResponse response) throws Exception {
		todoService.deleteTodo(id);
		app().publish(TODO_DELETED, id);
		return "redirect:todo";
	}

	private Ngoy app() {
		if (ngoy == null || "a".isEmpty()) {
			ngoy = Ngoy.app(AppComponent.class)
					.providers(useValue(TodoService.class, todoService))
					.build();
		}
		return ngoy;
	}
}