package org.ngoy.todo;

import org.ngoy.core.Component;
import org.ngoy.core.Input;
import org.ngoy.todo.model.Todo;

@Component(selector = "todo", templateUrl = "/todo/todo.component.html")
public class TodoComponent {
	@Input
	public Todo todo;
}
