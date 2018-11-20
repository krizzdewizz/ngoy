package ngoy.core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

/**
 * experimental.
 * <p>
 * Events are all synchronous. Upon {@link OnInit}, components have the chance
 * to {@link #subscribe(Object, Consumer)} or {@link #publish(Object, Object)}.
 * Just before the rendering phase, when all components have
 * subscribed/published, the event queue is flushed and the events are sent to
 * subscribers. Their event handlers will update the state which is then
 * rendered.
 * 
 * @author krizz
 */
public class Events {
	private static class Event {
		Object topic;
		Object payload;
	}

	private final Map<Object, Set<Consumer<?>>> subscriptions = new HashMap<>();
	private final List<Event> queue = new ArrayList<>();

	public <T> void subscribe(Object token, Consumer<T> consumer) {
		Set<Consumer<?>> set = subscriptions.get(token);
		if (set == null) {
			set = new LinkedHashSet<>();
			subscriptions.put(token, set);
		}
		set.add(consumer);
	}

	public void unsubscribe(Object topic) {
		subscriptions.remove(topic);
	}

	public <T> void unsubscribe(Object topic, Consumer<T> consumer) {
		Set<Consumer<?>> set = subscriptions.get(topic);
		if (set != null) {
			set.remove(consumer);
			if (set.isEmpty()) {
				subscriptions.remove(topic);
			}
		}
	}

	public <T> void publish(Object topic, T payload) {
		Event e = new Event();
		e.topic = topic;
		e.payload = payload;
		queue.add(e);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void tick() {
		for (Event e : new ArrayList<>(queue)) {
			Set<Consumer<?>> set = subscriptions.get(e.topic);
			if (set != null) {
				for (Consumer consumer : set) {
					consumer.accept(e.payload);
				}
			}
			queue.remove(e);
		}
		subscriptions.clear();
	}
}
