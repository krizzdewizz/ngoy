package org.ngoy.core;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class Events {
	private Map<String, Consumer<?>> map = new HashMap<>();

	public <T> void subscribe(String token, Consumer<T> c) {
		map.put(token, c);
	}

	public void unsubscribe(String token) {
		map.remove(token);
	}

	@SuppressWarnings("unchecked")
	public <T> void post(String token, T payload) {
		Consumer<T> consumer = (Consumer<T>) map.get(token);
		if (consumer != null) {
			consumer.accept(payload);
		}
	}
}
