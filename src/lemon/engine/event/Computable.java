package lemon.engine.event;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

public class Computable<T> {
    private final Observable<T> value;
    private final AtomicBoolean requested;
    private final Consumer<Computable<T>> computer;
    private final Object lock = new Object();

    public Computable(Consumer<Computable<T>> computer) {
        this.value = new Observable<>(null);
        this.requested = new AtomicBoolean(false);
        this.computer = computer;
    }

    public void compute() {
        synchronized (lock) {
            this.value.onSet().callListeners(this.value.getValue()); // TODO: we need a way to lazily call the listeners?
        }
    }

    public void compute(T value) {
        //requested.set(false);
        synchronized (lock) {
            this.value.setValue(value);
        }
    }

    public void compute(UnaryOperator<T> operator, Supplier<T> supplier) {
        //requested.set(false);
        synchronized (lock) {
            T value = this.value.getValue();
            if (value == null) {
                value = supplier.get();
            } else {
                value = operator.apply(value);
            }
            this.value.setValue(value);
        }
    }

    public Optional<T> getValue() {
        return Optional.ofNullable(this.value.getValue());
    }

    public void useOrRequest(Consumer<T> consumer) {
        T value = this.value.getValue();
        if (value != null) {
            consumer.accept(value);
        }
    }

    public T getValueOrThrow() {
        T value = this.value.getValue();
        if (value == null) {
            throw new IllegalStateException("Computable has not been computed");
        } else {
            return value;
        }
    }

    /**
     * @param runnable Called only a maximum of one time
     */
    public void whenCalculated(Runnable runnable) {
        this.whenCalculated(x -> runnable.run());
    }
    /**
     * @param consumer Called only a maximum of one time
     */
    public void whenCalculated(Consumer<T> consumer) {
        synchronized (lock) {
            T value = this.value.getValue();
            if (value == null) {
                this.value.onSet().addOnce(consumer);
            } else {
                consumer.accept(value);
            }
        }
    }

    /**
     * @param consumer Called whenever value gets recomputed
     */
    public void then(Consumer<? super T> consumer) {
        if (!requested.getAndSet(true)) {
            computer.accept(this);
        }
        synchronized (lock) {
            this.value.onSet().add(consumer);
            if (this.value.getValue() != null) {
                consumer.accept(this.value.getValue());
            }
        }
    }
    public <U> Computable<U> then(Function<? super T, ? extends U> function) {
        Computable<T> self = this;
        return new Computable<>(lazy -> {
            self.then((Consumer<? super T>) value -> lazy.compute(function.apply(value)));
        });
    }

    public static <T, U> Computable<U> all(List<Computable<T>> computables, Consumer<Computable<U>> computer) {
        return new Computable<>(resultComputable -> {
            AtomicInteger counter = new AtomicInteger();
            for (Computable<T> computable : computables) {
                AtomicBoolean incremented = new AtomicBoolean(false);
                computable.then(value -> {
                    int current = incremented.getAndSet(true) ? counter.get() : counter.incrementAndGet();
                    if (current == computables.size()) {
                        computer.accept(resultComputable);
                    }
                });
            }
        });
    }
}
