package org.ngoy.todo.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

public class Todo {

	public final String text;
	public final String id;
	public final String created;

	public Todo(String text) {
		this.id = UUID.randomUUID()
				.toString();
		this.created = DateTimeFormatter.ISO_DATE.format(LocalDateTime.now());
		this.text = text;
	}

}
