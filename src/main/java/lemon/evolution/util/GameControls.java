package lemon.evolution.util;

import lemon.engine.event.Observable;

public interface GameControls<T> {
	public Observable<Boolean> activated(T control);

	public default boolean isActivated(T control) {
		return activated(control).getValue();
	}
}
