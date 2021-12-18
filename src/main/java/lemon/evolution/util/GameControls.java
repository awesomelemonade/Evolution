package lemon.evolution.util;

import lemon.engine.event.EventWith;
import lemon.futility.Observable;
import lemon.engine.toolbox.Disposable;

import java.util.function.Consumer;
import java.util.function.Function;

public interface GameControls<T, U> {
	public Observable<Boolean> activated(T control);

	public default Disposable onActivated(T control, Runnable runnable) {
		return activated(control).onChangeTo(true, runnable);
	}

	public default boolean isActivated(T control) {
		return activated(control).getValue();
	}

	public <V> void addCallback(Function<U, EventWith<V>> inputEvent, Consumer<? super V> callback);
}
