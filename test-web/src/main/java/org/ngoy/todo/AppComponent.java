package org.ngoy.todo;

import java.util.List;

import org.ngoy.core.Component;
import org.ngoy.core.Events;
import org.ngoy.core.Inject;
import org.ngoy.core.NgModule;
import org.ngoy.core.OnInit;
import org.ngoy.todo.model.Todo;
import org.ngoy.todo.services.TodoService;

@Component(selector = "xxx", templateUrl = "/todo/app.component.html")
@NgModule(declarations = { TodoComponent.class }, providers = { TodoService.class })
public class AppComponent implements OnInit {
	public final String appName = "Todo";

	@Inject
	public TodoService todoService;

	@Inject
	public Events events;

	@Override
	public void ngOnInit() {
		events.<String>subscribe("todo.add", text -> todoService.addTodo(text));
		events.<String>subscribe("todo.delete", id -> todoService.deleteTodo(id));
	}

	public List<Todo> getTodos() {
		return todoService.getTodos();
	}

}
