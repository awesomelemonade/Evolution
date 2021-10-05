package lemon.evolution.util;

import lemon.engine.event.EventWith;
import lemon.engine.event.Observable;

import java.util.function.Consumer;
import java.util.function.Function;

public interface GameControls<T, U> {
	public Observable<Boolean> activated(T control);

	public default boolean isActivated(T control) {
		return activated(control).getValue();
	}

	public <V> void addCallback(Function<U, EventWith<V>> inputEvent, Consumer<? super V> callback);
}
