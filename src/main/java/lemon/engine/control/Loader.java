package lemon.engine.control;

import lemon.engine.toolbox.Disposable;

public interface Loader extends Disposable {
	public void load();

	public float getProgress();

	public default boolean isCompleted() {
		return getProgress() >= 1f;
	}

	public default String getDescription() {
		return this.toString();
	}

	public default void dispose() {
		// Do Nothing
	}
}
