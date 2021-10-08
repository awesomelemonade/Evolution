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

	@CheckReturnValue
	public Disposable onChangeAndRun(Consumer<? super T> listener) {
		listener.accept(value);
		return onChange.add(listener);
	}

	@CheckReturnValue
	public Disposable onChangeTo(T value, Runnable runnable) {
		return onChange.add(to -> {
			if (to.equals(value)) {
				runnable.run();
			}
		});
	}

	public static Observable<Boolean> ofAnd(Observable<Boolean> a, Observable<Boolean> b, Consumer<Disposable> disposer) {
		var ret = new Observable<>(a.getValue() && b.getValue());
		disposer.accept(a.onChange(condition -> ret.setValue(condition && b.getValue())));
		disposer.accept(b.onChange(condition -> ret.setValue(condition && a.getValue())));
		return ret;
	}
}
