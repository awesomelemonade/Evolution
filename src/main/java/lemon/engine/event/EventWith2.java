package lemon.engine.event;

import com.google.errorprone.annotations.CheckReturnValue;
import lemon.engine.toolbox.Disposable;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class EventWith2<T, U> {
	private final Map<Object, BiConsumer<? super T, ? super U>> listeners = new LinkedHashMap<>();
	private BiConsumer<? super T, ? super U>[] cachedListeners;

	/**
	 * @param listener Listener
	 * @return Remover
	 */
	@CheckReturnValue
	public Disposable add(BiConsumer<? super T, ? super U> listener) {
		Object key = new Object();
		cachedListeners = null;
		listeners.put(key, listener);
		return () -> {
			cachedListeners = null;
			listeners.remove(key);
		};
	}

	@CheckReturnValue
	public Disposable add(Consumer<? super T> listener) {
		return this.add((x, y) -> listener.accept(x));
	}

	@SuppressWarnings("unchecked")
	public void callListeners(T arg, U arg2) {
		if (cachedListeners == null) {
			cachedListeners = listeners.values().toArray(BiConsumer[]::new);
		}
		for (var listener : cachedListeners) {
			listener.accept(arg, arg2);
		}
	}
}
