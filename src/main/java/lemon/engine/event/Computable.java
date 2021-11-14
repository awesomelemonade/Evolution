package lemon.engine.event;

import lemon.engine.toolbox.Lazy;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

public class Computable<T> {
	public boolean currentlyComputing = false;
	public boolean needsUpdate = true;
	private final Consumer<Computable<T>> computer;
	private final List<Computable<?>> dependers = Collections.synchronizedList(new ArrayList<>());
	private final OneTimeEventWith<T> whenCalculated = new OneTimeEventWith<>();
	private T value = null;

	public Computable(Consumer<Computable<T>> computer) {
		this.computer = computer;
	}

	private void propagateNeedsUpdate() {
		synchronized (dependers) {
			for (var depender : dependers) {
				depender.needsUpdate = true;
				depender.propagateNeedsUpdate();
			}
		}
	}

	public void addDepender(Computable<?> depender) {
		this.dependers.add(depender);
	}

	public synchronized void compute() {
		propagateNeedsUpdate();
		whenCalculated.callListeners(value);
		currentlyComputing = false; // allow computable to be compute()ed again
	}

	public synchronized void compute(T value) {
		this.value = value;
		this.compute();
	}

	/**
	 * Applies the operator if value exists, otherwise retrieves from supplier
	 */
	public synchronized void compute(UnaryOperator<T> operator, Supplier<T> supplier) {
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
	public synchronized void compute(Consumer<T> consumer, Supplier<T> supplier) {
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
	public synchronized void compute(Consumer<T> consumer) {
		if (value != null) {
			consumer.accept(value);
			this.compute();
		}
	}

	public synchronized Optional<T> getValue() {
		return Optional.ofNullable(this.value);
	}
	public synchronized T getValueOrThrow() {
		if (value == null) {
			throw new IllegalStateException();
		}
		return value;
	}

	public synchronized T getValueOrThrow(Supplier<? extends RuntimeException> exceptionSupplier) {
		if (value == null) {
			throw exceptionSupplier.get();
		}
		return value;
	}

	public synchronized void request() {
		if (needsUpdate) {
			if (!currentlyComputing) {
				currentlyComputing = true;
				needsUpdate = false;
				computer.accept(this);
			}
		}
	}

	public synchronized Optional<T> requestAndGetValue() {
		request();
		return getValue();
	}

	/**
	 * Requests an update (if needed) and calls the callback when it is calculated
	 */
	public synchronized void request(Consumer<T> whenCalculated) {
		if (needsUpdate) {
			this.whenCalculated.add(whenCalculated);
			if (!currentlyComputing) {
				currentlyComputing = true;
				needsUpdate = false;
				computer.accept(this);
			}
		} else {
			// we don't need an update
			if (currentlyComputing) {
				this.whenCalculated.add(whenCalculated);
			} else {
				whenCalculated.accept(value);
			}
		}
	}

	// two ways of generating a computable from another computable
	// singular computable -> computable, multiple computable -> computable
	public <U> Computable<U> then(Executor executor, BiConsumer<Computable<U>, ? super T> consumer) {
		return then((computable, value) -> executor.execute(() -> consumer.accept(computable, value)));
	}

	public <U> Computable<U> then(BiConsumer<Computable<U>, ? super T> consumer) {
		Computable<T> self = this;
		Computable<U> ret = new Computable<>(computer -> {
			self.request(value -> {
				consumer.accept(computer, value);
			});
		});
		this.dependers.add(ret);
		return ret;
	}

	public static <T> Computable<T> all(Executor executor, Supplier<List<? extends Computable<?>>> lazyComputables, Consumer<Computable<T>> consumer) {
		return all(lazyComputables, computable -> executor.execute(() -> consumer.accept(computable)));
	}

	public static <T> Computable<T> all(Supplier<List<? extends Computable<?>>> lazyComputables, Consumer<Computable<T>> computer) {
		return all(new Lazy<>(lazyComputables), computer);
	}

	public static <T> Computable<T> all(Lazy<List<? extends Computable<?>>> lazyComputables, Consumer<Computable<T>> computer) {
		Computable<T> ret = new Computable<>(resultComputable -> {
			AtomicInteger counter = new AtomicInteger();
			var computables = lazyComputables.get();
			for (var computable : computables) {
				AtomicBoolean incremented = new AtomicBoolean(false);
				computable.request(value -> {
					int current = incremented.getAndSet(true) ? counter.get() : counter.incrementAndGet();
					if (current == computables.size()) {
						computer.accept(resultComputable);
					}
				});
			}
		});
		lazyComputables.onComputed().add(computables -> computables.forEach(computable -> computable.addDepender(ret)));
		return ret;
	}
}
