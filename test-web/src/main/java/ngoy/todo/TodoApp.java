package ngoy.todo;

import static ngoy.core.Util.isSet;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import ngoy.core.Component;
import ngoy.core.Inject;
import ngoy.core.NgModule;
import ngoy.core.OnDestroy;
import ngoy.forms.FormsModule;
import ngoy.todo.components.TodoComponent;
import ngoy.todo.model.Todo;
import ngoy.todo.services.TodoService;

@Component(selector = "", templateUrl = "app.component.html", styleUrls = { "app.component.css" })
@NgModule(imports = { FormsModule.class }, declarations = { TodoComponent.class })
@Controller
public class TodoApp implements OnDestroy {
	private static final String REDIRECT_HOME = "redirect:/todo";

	public final String appName = "Todo";

	@Inject
	public TodoService todoService;

	public String deletedTodo;
	public boolean textRequired;

	@Override
	public void ngOnDestroy() {
		deletedTodo = null;
		textRequired = false;
	}

	public List<Todo> getTodos() {
		return todoService.getTodos();
	}

	@PostMapping("add")
	public String addTodo(@RequestParam("text") String text) throws Exception {
		boolean ok = isSet(text);
		textRequired = !ok;
		if (ok) {
			todoService.addTodo(text);
		}
		return REDIRECT_HOME;
	}

	@PostMapping("delete")
	public String deleteTodo(@RequestParam("id") String id) throws Exception {
		todoService.getTodo(id)
				.ifPresent(todo -> {
					deletedTodo = todo.text;
					todoService.deleteTodo(id);
				});
		return REDIRECT_HOME;
	}
}
