package org.ngoy.todo;

import static org.ngoy.todo.TodoEvent.TODO_DELETED;

import java.util.List;
import java.util.function.Consumer;

import org.ngoy.core.Component;
import org.ngoy.core.Events;
import org.ngoy.core.Inject;
import org.ngoy.core.NgModule;
import org.ngoy.core.OnDestroy;
import org.ngoy.core.OnInit;
import org.ngoy.todo.model.Todo;
import org.ngoy.todo.services.TodoService;

@Component(selector = "", templateUrl = "/todo/app.component.html")
@NgModule(declarations = { TodoComponent.class })
public class AppComponent implements OnInit, OnDestroy {
	public final String appName = "Todo";

	@Inject
	public TodoService todoService;

	@Inject
	public Events events;

	public boolean deleted;

	private Consumer<String> setDeleted = unused -> deleted = true;

	public boolean isDeleted() {
		return deleted;
	}

	@Override
	public void ngOnInit() {
		deleted = false;
		events.<String>subscribe(TODO_DELETED, setDeleted);
	}

	@Override
	public void ngOnDestroy() {
		events.<String>unsubscribe(TODO_DELETED, setDeleted);
	}

	public List<Todo> getTodos() {
		return todoService.getTodos();
	}
}
