package org.ngoy.todo;

import java.util.List;

import javax.servlet.http.HttpServletResponse;

import org.ngoy.core.Component;
import org.ngoy.core.Events;
import org.ngoy.core.Inject;
import org.ngoy.core.NgModule;
import org.ngoy.core.OnInit;
import org.ngoy.core.Renderer;
import org.ngoy.todo.model.Todo;
import org.ngoy.todo.services.TodoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Component(selector = "", templateUrl = "app.component.html")
@NgModule(declarations = { TodoComponent.class, FormPostActionDirective.class })
@Controller
public class AppComponent implements OnInit {
	public final String appName = "Todo";

	@Autowired
	public TodoService todoService;

	@Inject
	public Renderer renderer;

	@Inject
	public Events events;

	public boolean deleted;

	@Override
	public void ngOnInit() {
		deleted = false;
		events.subscribe(TodoEvent.TODO_DELETED, unused -> {
			deleted = true;
		});
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
		todoService.addTodo(text);
		return "redirect:todo";
	}
}
