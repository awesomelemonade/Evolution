package lemon.engine.event;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Function;

public class AsyncLazy<T> {
    private EventWith<T> event;
    private AtomicBoolean requested;
    private Consumer<AsyncLazy<T>> loader;
    private T value;
    private final Object lock = new Object();

    public AsyncLazy(Runnable loader) {
        this.requested = new AtomicBoolean(false);
        this.loader = x -> loader.run();
        this.event = new EventWith<>();
        this.value = null;
    }
    public AsyncLazy(Consumer<AsyncLazy<T>> loader) {
        this.loader = loader;
    }

    public void resolve(T value) {
        if (this.value != null) {
            throw new IllegalStateException("Already calculated AsyncLazy");
        }
        synchronized (lock) {
            this.value = value;
            event.callListeners(value);
        }
    }

    // Terminal call
    public void then(Consumer<? super T> consumer) {
        if (!requested.getAndSet(true)) {
            loader.accept(this); // invariant: loader should only be called once for each value = null
        }
        synchronized (lock) {
            event.add(consumer);
            if (value != null) {
                consumer.accept(value);
            }
        }
    }
    public <U> AsyncLazy<U> then(Function<? super T, ? extends U> function) {
        AsyncLazy<T> self = this;
        return new AsyncLazy<>(lazy -> {
            self.then((Consumer<? super T>) value -> lazy.resolve(function.apply(value)));
        });
    }

    @SafeVarargs
    @SuppressWarnings("unchecked")
    public static <T> AsyncLazy<T[]> all(AsyncLazy<T>... lazies) {
       return new AsyncLazy<>(resultLazy -> {
            AtomicInteger counter = new AtomicInteger();
            T[] result = (T[]) new Object[lazies.length]; // doesn't really work because of memory consistency
            for (int i = 0; i < lazies.length; i++) {
                int finalI = i;
                lazies[i].then(value -> {
                    result[finalI] = value;
                    if (counter.incrementAndGet() == lazies.length) {
                        resultLazy.resolve(result);
                    }
                });
            }
        });
    }
}
