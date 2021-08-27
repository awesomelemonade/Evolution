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

public class BetterComputable<T> {
	private final AtomicBoolean requested = new AtomicBoolean(false);
	private final AtomicBoolean needsUpdate = new AtomicBoolean(false);
	private final Consumer<BetterComputable<T>> computer;
	private final List<BetterComputable<?>> dependers = new ArrayList<>();
	private final OneTimeEventWith<T> whenCalculated = new OneTimeEventWith<>();
	private T value = null;

	public BetterComputable(Consumer<BetterComputable<T>> computer) {
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

	public Optional<T> getValue() {
		if (needsUpdate.getAndSet(false)) {
			if (!requested.getAndSet(true)) {
				computer.accept(this);
			} else {
				// we need another update after computer is finished
				needsUpdate.set(true);
			}
		}
		return Optional.ofNullable(this.value);
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

	public <U> BetterComputable<U> then(BiConsumer<BetterComputable<U>, ? super T> consumer) {
		BetterComputable<T> self = this;
		BetterComputable<U> ret = new BetterComputable<>(computer -> {
			self.requestForUpdate(value -> {
				consumer.accept(computer, value);
			});
		});
		this.dependers.add(ret);
		return ret;
	}

	public static <T, U> BetterComputable<U> all(Supplier<List<BetterComputable<T>>> lazyComputables, Consumer<BetterComputable<U>> computer) {
		return all(new Lazy<>(lazyComputables), computer);
	}

	public static <T, U> BetterComputable<U> all(Lazy<List<BetterComputable<T>>> lazyComputables, Consumer<BetterComputable<U>> computer) {
		BetterComputable<U> ret = new BetterComputable<>(resultComputable -> {
			AtomicInteger counter = new AtomicInteger();
			var computables = lazyComputables.get();
			for (BetterComputable<T> computable : computables) {
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
