package org.ngoy.core;

import java.util.function.Consumer;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

public class EventsTest {

	private static final Object TOKEN = new Object();
	private Events events;

	@Before
	public void beforeEach() {
		events = new Events();
	}

	@Test
	public void testSubscribe() {
		Consumer<String> consumer = stringConsumerMock();
		events.subscribe(TOKEN, consumer);
		events.publish(TOKEN, "hello");
		events.tick();
		Mockito.verify(consumer)
				.accept("hello");
	}

	@Test
	public void testUnsubscribe() {
		Consumer<String> consumer1 = stringConsumerMock();
		Consumer<String> consumer2 = stringConsumerMock();
		events.subscribe(TOKEN, consumer1);
		events.subscribe(TOKEN, consumer2);
		events.unsubscribe(TOKEN, consumer1);
		events.publish(TOKEN, "hello");
		events.tick();
		Mockito.verifyZeroInteractions(consumer1);
		Mockito.verify(consumer2)
				.accept("hello");
	}

	@Test
	public void testUnsubscribeAll() {
		Consumer<String> consumer1 = stringConsumerMock();
		Consumer<String> consumer2 = stringConsumerMock();
		events.subscribe(TOKEN, consumer1);
		events.subscribe(TOKEN, consumer2);
		events.unsubscribe(TOKEN);
		events.publish(TOKEN, "hello");
		events.tick();
		Mockito.verifyZeroInteractions(consumer1);
		Mockito.verifyZeroInteractions(consumer2);
	}

	@SuppressWarnings("unchecked")
	private Consumer<String> stringConsumerMock() {
		return Mockito.mock(Consumer.class);
	}
}
