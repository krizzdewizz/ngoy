package org.ngoy.todo;

import static org.ngoy.core.Util.isSet;

import java.util.List;

import org.ngoy.core.Component;
import org.ngoy.core.Events;
import org.ngoy.core.Inject;
import org.ngoy.core.NgModule;
import org.ngoy.core.OnDestroy;
import org.ngoy.core.Renderer;
import org.ngoy.forms.FormsModule;
import org.ngoy.todo.components.TodoComponent;
import org.ngoy.todo.model.Todo;
import org.ngoy.todo.services.TodoService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Component(selector = "", templateUrl = "app.component.html", styleUrls = { "app.component.css" })
@NgModule(imports = { FormsModule.class }, declarations = { TodoComponent.class })
@Controller
public class AppComponent implements OnDestroy {
	private static final String REDIRECT_HOME = "redirect:/todo";

	public final String appName = "Todo";

	@Inject
	public TodoService todoService;

	@Inject
	public Renderer renderer;

	@Inject
	public Events events;

	public boolean deleted;
	public boolean textRequired;

	@Override
	public void ngOnDestroy() {
		deleted = false;
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
		deleted = true;
		todoService.deleteTodo(id);
		return REDIRECT_HOME;
	}
}
