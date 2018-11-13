package org.ngoy.todo.services;

import static java.util.Arrays.asList;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.ngoy.todo.model.Todo;
import org.springframework.stereotype.Service;

@Service
public class TodoService {

	// the 'database' or 'repository'
	private final List<Todo> todos = new ArrayList<>(asList(new Todo("wash car"), new Todo("cook dinner")));

	public List<Todo> getTodos() {
		return todos;
	}

	public Todo addTodo(String text) {
		Todo todo = new Todo(text);
		todos.add(0, todo);
		return todo;
	}

	public void deleteTodo(String id) {
		todos.removeIf(todo -> todo.id.equals(id));
	}

	public Optional<Todo> getTodo(String id) {
		return todos.stream()
				.filter(it -> it.id.equals(id))
				.findFirst();
	}
}
