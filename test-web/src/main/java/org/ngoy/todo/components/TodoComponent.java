package org.ngoy.todo.components;

import org.ngoy.core.Component;
import org.ngoy.core.Input;
import org.ngoy.todo.model.Todo;
import org.springframework.stereotype.Controller;

@Component(selector = "todo", templateUrl = "todo.component.html", styleUrls = { "todo.component.css" })
@Controller
public class TodoComponent {
	@Input
	public Todo todo;

	@Input
	public String deleteAction;
}
