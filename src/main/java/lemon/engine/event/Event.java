package lemon.engine.event;

import java.util.LinkedHashMap;
import java.util.Map;

public class Event {
	private final Map<Object, Runnable> listeners = new LinkedHashMap<>(); // Map used to allow duplicates
	private Runnable[] cachedListeners;

	/**
	 * @param listener Listener
	 * @return Remover
	 */
	public Runnable add(Runnable listener) {
		Object key = new Object();
		cachedListeners = null;
		listeners.put(key, listener);
		return () -> {
			cachedListeners = null;
			listeners.remove(key);
		};
	}

	public void callListeners() {
		if (cachedListeners == null) {
			cachedListeners = listeners.values().toArray(Runnable[]::new);
		}
		for (Runnable runnable : cachedListeners) {
			runnable.run();
		}
	}
}
