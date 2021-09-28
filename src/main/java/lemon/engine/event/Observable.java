package lemon.engine.event;

import com.google.errorprone.annotations.CheckReturnValue;
import lemon.engine.toolbox.Disposable;

import java.util.function.Consumer;

public class Observable<T> {
	private final EventWith<T> onSet = new EventWith<>();
	private final EventWith<T> onChange = new EventWith<>();
	private T value;

	public Observable(T value) {
		this.value = value;
	}

	public void setValue(T value) {
		boolean changed = !this.value.equals(value);
		this.value = value;
		onSet.callListeners(value);
		if (changed) {
			onChange.callListeners(value);
		}
	}

	public T getValue() {
		return value;
	}

	public EventWith<T> onSet() {
		return onSet;
	}

	@CheckReturnValue
	public Disposable onSet(Consumer<? super T> listener) {
		return onSet.add(listener);
	}

	public EventWith<T> onChange() {
		return onChange;
	}

	@CheckReturnValue
	public Disposable onChange(Consumer<? super T> listener) {
		return onChange.add(listener);
	}
}
