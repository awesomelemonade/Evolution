package lemon.engine.toolbox;

public interface Disposable extends AutoCloseable {
	public void dispose();

	@Override
	public default void close() {
		this.dispose();
	}

	public static Disposable of(Disposable... disposables) {
		return () -> {
			for (Disposable disposable : disposables) {
				disposable.dispose();
			}
		};
	}
}
