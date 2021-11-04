package lemon.engine.event;

import com.google.errorprone.annotations.CheckReturnValue;
import lemon.engine.toolbox.Disposable;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Consumer;

public class EventWith<T> {
	private final Map<Object, Consumer<? super T>> listeners = new LinkedHashMap<>();
	private Consumer<? super T>[] cachedListeners;

	/**
	 * @param listener Listener
	 * @return Remover
	 */
	@CheckReturnValue
	public Disposable add(Consumer<? super T> listener) {
		Object key = new Object();
		cachedListeners = null;
		listeners.put(key, listener);
		return () -> {
			cachedListeners = null;
			listeners.remove(key);
		};
	}

	@SuppressWarnings("unchecked")
	public void callListeners(T arg) {
		if (cachedListeners == null) {
			cachedListeners = listeners.values().toArray(Consumer[]::new);
		}
		for (Consumer<? super T> listener : cachedListeners) {
			listener.accept(arg);
		}
	}
}
