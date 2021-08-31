package lemon.engine.event;

import java.util.LinkedHashMap;
import java.util.Map;

public class SynchronizedEvent {
	private Map<Object, Runnable> listeners;
	private Runnable[] cachedListeners;
	private final Object lock = new Object();

	public SynchronizedEvent() {
		listeners = new LinkedHashMap<>(); // Map is used so we allow duplicates
	}

	/**
	 * @param listener Listener
	 * @return Remover
	 */
	public Runnable addListener(Runnable listener) {
		Object key = new Object();
		synchronized (lock) {
			cachedListeners = null;
			listeners.put(key, listener);
		}
		return () -> {
			synchronized (lock) {
				cachedListeners = null;
				listeners.remove(key);
			}
		};
	}

	public void callListeners() {
		synchronized (lock) {
			if (cachedListeners == null) {
				cachedListeners = listeners.values().toArray(Runnable[]::new);
			}
		}
		for (Runnable listener : cachedListeners) {
			listener.run();
		}
	}
}
