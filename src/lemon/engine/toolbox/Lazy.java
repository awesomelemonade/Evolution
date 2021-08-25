package lemon.engine.toolbox;

import java.util.function.Supplier;

public interface Lazy<T> extends Supplier<T> {
	public static <T> Lazy<T> of(Supplier<T> initializer) {
		return new Lazy<>() {
			T value;
			@Override
			public T get() {
				if (value == null) {
					value = initializer.get();
				}
				return value;
			}
		};
	}
}
