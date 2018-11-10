package org.ngoy.todo.components;

import static org.ngoy.todo.TodoEvent.TODO_DELETED;

import org.ngoy.core.Component;
import org.ngoy.core.Events;
import org.ngoy.core.Inject;
import org.ngoy.core.Input;
import org.ngoy.todo.model.Todo;
import org.ngoy.todo.services.TodoService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Component(selector = "todo", templateUrl = "todo.component.html", styleUrls = { "todo.component.css" })
@Controller
public class TodoComponent {
	@Input
	public Todo todo;

	@Inject
	public TodoService todoService;

	@Inject
	public Events events;

	public final String todoDeleteAction = "/tododelete";

	@PostMapping("/tododelete")
	public String deleteTodo(@RequestParam("id") String id) throws Exception {
		todoService.deleteTodo(id);
		events.publish(TODO_DELETED, null);
		return "redirect:todo";
	}
}
