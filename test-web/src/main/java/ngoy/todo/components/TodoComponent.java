package ngoy.todo.components;

import org.springframework.stereotype.Controller;

import ngoy.core.Component;
import ngoy.core.Input;
import ngoy.todo.model.Todo;

@Component(selector = "todo", templateUrl = "todo.component.html", styleUrls = { "todo.component.css" })
@Controller
public class TodoComponent {
	@Input
	public Todo todo;

	@Input
	public String deleteAction;
}
