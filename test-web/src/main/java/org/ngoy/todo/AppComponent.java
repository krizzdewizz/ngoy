package org.ngoy.todo;

import static org.ngoy.todo.TodoEvent.ADD_TODO;
import static org.ngoy.todo.TodoEvent.DELETE_TODO;

import java.util.List;

import org.ngoy.core.Component;
import org.ngoy.core.Events;
import org.ngoy.core.Inject;
import org.ngoy.core.NgModule;
import org.ngoy.core.OnInit;
import org.ngoy.todo.model.Todo;
import org.ngoy.todo.services.TodoService;

@Component(selector = "", templateUrl = "/todo/app.component.html")
@NgModule(declarations = { TodoComponent.class })
public class AppComponent implements OnInit {
	public final String appName = "Todo";

	@Inject
	public TodoService todoService;

	@Inject
	public Events events;

	public boolean deleted;

	@Override
	public void ngOnInit() {
		deleted = false;
		events.<String>subscribe(ADD_TODO, todoService::addTodo);
		events.<String>subscribe(DELETE_TODO, id -> {
			todoService.deleteTodo(id);
			deleted = true;
		});
	}

	public List<Todo> getTodos() {
		return todoService.getTodos();
	}
}
