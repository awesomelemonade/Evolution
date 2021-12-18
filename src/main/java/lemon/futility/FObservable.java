package lemon.futility;

import lemon.engine.event.EventWith;
import lemon.engine.toolbox.Disposable;

import java.util.function.Consumer;

public class FObservable<T> implements Observable<T> {
	private final EventWith<T> onChange = new EventWith<>();
	private T value;

	public FObservable(T value) {
		this.value = value;
	}

	public void setValue(T value) {
		boolean changed = !this.value.equals(value);
		this.value = value;
		if (changed) {
			onChange.callListeners(value);
		}
	}

	@Override
	public T getValue() {
		return value;
	}

	@Override
	public EventWith<T> onChange() {
		return onChange;
	}

	public static Observable<Boolean> ofAnd(Observable<Boolean> a, Observable<Boolean> b, Consumer<Disposable> disposer) {
		var ret = new FObservable<>(a.getValue() && b.getValue());
		disposer.accept(a.onChange(condition -> ret.setValue(condition && b.getValue())));
		disposer.accept(b.onChange(condition -> ret.setValue(condition && a.getValue())));
		return ret;
	}

	public static Observable<Boolean> ofNot(Observable<Boolean> observable, Consumer<Disposable> disposer) {
		var ret = new FObservable<>(!observable.getValue());
		disposer.accept(observable.onChange(condition -> ret.setValue(!condition)));
		return ret;
	}
}
