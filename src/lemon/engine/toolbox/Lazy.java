package lemon.engine.toolbox;

import lemon.engine.event.EventWith;

import java.util.function.Supplier;

public class Lazy<T> implements Supplier<T> {
	private final EventWith<T> onComputed = new EventWith<>();
	private final Supplier<T> initializer;
	private T value;

	public Lazy(Supplier<T> initializer) {
		this.initializer = initializer;
	}

	@Override
	public T get() {
		if (value == null) {
			value = initializer.get();
			onComputed.callListeners(value);
		}
		return value;
	}

	public EventWith<T> onComputed() {
		return onComputed;
	}
}
