package lemon.engine.event;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class EventTest {
	@Test
	public void testListener() {
		var listener = new TestListener();
		var event = new Event();
		// Test add not firing listener
		event.add(listener);
		listener.assertNeverFired();
		// Test call listeners
		event.callListeners();
		listener.assertFired(1);
		// Test calling listeners multiple times
		event.callListeners();
		event.callListeners();
		listener.assertFired(2);
		// Test registering the same listener
		var removable = event.add(listener);
		event.callListeners();
		listener.assertFired(2);
		// Test removing listeners
		removable.run();
		event.callListeners();
		listener.assertFired(1);
	}
}