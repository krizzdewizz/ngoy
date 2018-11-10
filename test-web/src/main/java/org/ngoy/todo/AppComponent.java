package org.ngoy.todo;

import static org.ngoy.core.Util.isSet;

import java.util.List;

import javax.servlet.http.HttpServletResponse;

import org.ngoy.core.Component;
import org.ngoy.core.Events;
import org.ngoy.core.Inject;
import org.ngoy.core.NgModule;
import org.ngoy.core.OnDestroy;
import org.ngoy.core.OnInit;
import org.ngoy.core.Renderer;
import org.ngoy.forms.FormsModule;
import org.ngoy.todo.components.TodoComponent;
import org.ngoy.todo.model.Todo;
import org.ngoy.todo.services.TodoService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Component(selector = "", templateUrl = "app.component.html", styleUrls = { "app.component.css" })
@NgModule(imports = { FormsModule.class }, declarations = { TodoComponent.class })
@Controller
public class AppComponent implements OnInit, OnDestroy {
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
	public void ngOnInit() {
		deleted = false;
		events.subscribe(TodoEvent.TODO_DELETED, unused -> {
			deleted = true;
		});
	}

	@Override
	public void ngOnDestroy() {
		textRequired = false;
	}

	public List<Todo> getTodos() {
		return todoService.getTodos();
	}

	@GetMapping(path = "/todo")
	public void todos(HttpServletResponse response) throws Exception {
		renderer.render(response.getOutputStream());
	}

	public final String todoAddAction = "/todoadd";

	@PostMapping("/todoadd")
	public String addTodo(@RequestParam("text") String text) throws Exception {
		boolean ok = isSet(text);
		textRequired = !ok;
		if (ok) {
			todoService.addTodo(text);
		}
		return "redirect:todo";
	}
}
