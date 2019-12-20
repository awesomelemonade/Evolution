package lemon.engine.control;

import lemon.engine.math.Percentage;

public interface Loader {
	public void load();
	public Percentage getPercentage();
	public default boolean isCompleted() {
		return getPercentage().isCompleted();
	}
}
