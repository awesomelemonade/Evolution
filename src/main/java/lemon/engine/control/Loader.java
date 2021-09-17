package lemon.engine.control;

public interface Loader {
	public void load();

	public float getProgress();

	public default boolean isCompleted() {
		return getProgress() >= 1f;
	}

	public default String getDescription() {
		return this.toString();
	}
}
