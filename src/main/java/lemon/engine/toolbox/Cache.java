package lemon.engine.toolbox;

import java.util.function.Supplier;

public class Cache<T> implements Supplier<T> {
	private final Supplier<T> supplier;
	private T cached;

	public Cache(Supplier<T> supplier) {
		this.supplier = supplier;
		this.cached = null;
	}

	public void invalidate() {
		cached = null;
	}

	@Override
	public T get() {
		if (cached == null) {
			cached = supplier.get();
		}
		return cached;
	}
}
