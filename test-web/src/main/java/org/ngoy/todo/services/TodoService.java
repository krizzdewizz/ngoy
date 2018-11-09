package org.ngoy.todo.services;

import static java.util.Arrays.asList;

import java.util.ArrayList;
import java.util.List;

import org.ngoy.todo.model.Todo;

public class TodoService {

	// the 'database' or 'repository'
	private final List<Todo> todos = new ArrayList<>(asList(new Todo("rauche noch einen letzten joint"), new Todo("gehe in's bett")));

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
}
