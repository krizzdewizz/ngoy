package org.ngoy.core;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

public class Events {
	private Map<Object, Set<Consumer<?>>> map = new HashMap<>();

	public <T> void subscribe(Object token, Consumer<T> consumer) {
		Set<Consumer<?>> set = map.get(token);
		if (set == null) {
			set = new LinkedHashSet<>();
			map.put(token, set);
		}
		set.add(consumer);
	}

	public void unsubscribe(Object topic) {
		map.remove(topic);
	}

	public void unsubscribe(Object topic, Consumer<?> consumer) {
		Set<Consumer<?>> set = map.get(topic);
		if (set != null) {
			set.remove(consumer);
			if (set.isEmpty()) {
				map.remove(topic);
			}
		}
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public <T> void publish(Object topic, T payload) {
		Set<Consumer<?>> set = map.get(topic);
		if (set != null) {
			for (Consumer consumer : set) {
				consumer.accept(payload);
			}
		}
	}
}
