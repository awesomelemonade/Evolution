package lemon.engine.toolbox;

import java.io.BufferedReader;
import java.io.IOException;

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

	public static Disposable ofBufferedReader(BufferedReader reader) {
		return () -> {
			try {
				reader.close();
			} catch (IOException e) {
				e.printStackTrace();
				throw new IllegalStateException(e);
			}
		};
	}
}
