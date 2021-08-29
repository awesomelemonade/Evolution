package lemon.engine.event;

public class Observable<T> {
	private EventWith<T> onSet = new EventWith<>();
	private EventWith<T> onChange = new EventWith<>();
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

	public EventWith<T> onChange() {
		return onChange;
	}
}
