package lemon.futility;

import com.google.errorprone.annotations.CheckReturnValue;
import lemon.engine.event.EventWith;
import lemon.engine.toolbox.Disposable;

import java.util.function.Consumer;
import java.util.function.Predicate;

public interface Observable<T> {
	public T getValue();

	public EventWith<T> onChange();

	@CheckReturnValue
	public default Disposable onChange(Consumer<? super T> listener) {
		return onChange().add(listener);
	}

	@CheckReturnValue
	public default Disposable onChangeAndRun(Consumer<? super T> listener) {
		listener.accept(getValue());
		return onChange().add(listener);
	}

	@CheckReturnValue
	public default Disposable onChangeTo(T value, Runnable runnable) {
		return onChange().add(to -> {
			if (to.equals(value)) {
				runnable.run();
			}
		});
	}

	@CheckReturnValue
	public default Disposable onChange(Predicate<T> predicate, Runnable runnable) {
		return onChange().add(to -> {
			if (predicate.test(to)) {
				runnable.run();
			}
		});
	}
}
