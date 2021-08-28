package lemon.engine.event;

import lemon.engine.toolbox.Lazy;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

public class Computable<T> {
	private final AtomicBoolean requested = new AtomicBoolean(false);
	private final AtomicBoolean needsUpdate = new AtomicBoolean(true);
	private final Consumer<Computable<T>> computer;
	private final List<Computable<?>> dependers = new ArrayList<>();
	private final OneTimeEventWith<T> whenCalculated = new OneTimeEventWith<>();
	private T value = null;

	public Computable(Consumer<Computable<T>> computer) {
		this.computer = computer;
	}

	public void compute() {
		for (var depender : dependers) {
			depender.needsUpdate.set(true);
		}
		// fire all the events for whenCalculated()
		whenCalculated.callListeners(value);
		requested.set(false); // allow computable to be compute()ed again
	}

	public void compute(T value) {
		this.value = value;
		this.compute();
	}

	/**
	 * Applies the operator if value exists, otherwise retrieves from supplier
	 */
	public void compute(UnaryOperator<T> operator, Supplier<T> supplier) {
		if (value == null) {
			value = supplier.get();
		} else {
			value = operator.apply(value);
		}
		this.compute();
	}

	/**
	 * Calls the consumer if value exists, otherwise retrieves from supplier
	 */
	public void compute(Consumer<T> consumer, Supplier<T> supplier) {
		if (value == null) {
			value = supplier.get();
		} else {
			consumer.accept(value);
		}
		this.compute();
	}

	/**
	 * Calls the consumer if value exists
	 */
	public void compute(Consumer<T> consumer) {
		if (value != null) {
			consumer.accept(value);
			this.compute();
		}
	}

	public Optional<T> getValue() {
		return Optional.ofNullable(this.value);
	}
	public T getValueOrThrow() {
		if (value == null) {
			throw new IllegalStateException();
		}
		return value;
	}

	public void request() {
		if (needsUpdate.getAndSet(false)) {
			if (!requested.getAndSet(true)) {
				computer.accept(this);
			} else {
				// we need another update after computer is finished
				needsUpdate.set(true);
			}
		}
	}

	public Optional<T> requestAndGetValue() {
		request();
		return getValue();
	}

	/**
	 * Requests an update (if needed) and calls the callback when it is calculated
	 */
	public void requestForUpdate(Consumer<T> whenCalculated) {
		if (needsUpdate.getAndSet(false)) {
			this.whenCalculated.add(whenCalculated);
			if (!requested.getAndSet(true)) {
				computer.accept(this);
			} else {
				needsUpdate.set(true);
			}
		} else {
			// value does not need to be updated
			whenCalculated.accept(value);
		}
	}

	// two ways of generating a computable from another computable

	public <U> Computable<U> then(BiConsumer<Computable<U>, ? super T> consumer) {
		Computable<T> self = this;
		Computable<U> ret = new Computable<>(computer -> {
			self.requestForUpdate(value -> {
				consumer.accept(computer, value);
			});
		});
		this.dependers.add(ret);
		return ret;
	}

	public static <T, U> Computable<U> all(Supplier<List<Computable<T>>> lazyComputables, Consumer<Computable<U>> computer) {
		return all(new Lazy<>(lazyComputables), computer);
	}

	public static <T, U> Computable<U> all(Lazy<List<Computable<T>>> lazyComputables, Consumer<Computable<U>> computer) {
		Computable<U> ret = new Computable<>(resultComputable -> {
			AtomicInteger counter = new AtomicInteger();
			var computables = lazyComputables.get();
			for (Computable<T> computable : computables) {
				AtomicBoolean incremented = new AtomicBoolean(false);
				computable.requestForUpdate(value -> {
					int current = incremented.getAndSet(true) ? counter.get() : counter.incrementAndGet();
					if (current == computables.size()) {
						computer.accept(resultComputable);
					}
				});
			}
		});
		lazyComputables.onComputed().add(computables -> computables.forEach(computable -> computable.dependers.add(ret)));
		return ret;
	}
}
