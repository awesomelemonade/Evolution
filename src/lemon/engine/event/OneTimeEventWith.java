package lemon.engine.event;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class OneTimeEventWith<T> {
	private final List<Consumer<T>> listeners = new ArrayList<>();

	public void add(Consumer<T> listener) {
		listeners.add(listener);
	}

	public void callListeners(T arg) {
		listeners.forEach(listener -> listener.accept(arg));
		listeners.clear();
	}
}
