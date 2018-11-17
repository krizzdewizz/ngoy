package ngoy.todo.model;

import java.time.LocalDateTime;
import java.util.UUID;

public class Todo {

	public final String text;
	public final String id;
	public final LocalDateTime created;

	public Todo(String text) {
		this.id = UUID.randomUUID()
				.toString();
		this.created = LocalDateTime.now();
		this.text = text;
	}

}
