package lemon.engine.event;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.*;

public class BetterComputable<T> {
    private T value = null;
    private final AtomicBoolean requested = new AtomicBoolean(false);
    private final AtomicBoolean needsUpdate = new AtomicBoolean(false);
    private final Consumer<BetterComputable<T>> computer;
    private final List<BetterComputable<?>> dependers = new ArrayList<>();
    private final OneTimeEventWith<T> whenCalculated = new OneTimeEventWith<>();

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
        if (!requested.getAndSet(true)) {
            // check if all of the computables below us have values AND if any of them changed, recompute




            computer.accept(this);
        }
        return Optional.ofNullable(this.value);
    }

    /**
     * Requests an update (if needed) and calls the callback when it is calculated
     */
    public void requestForUpdate(Consumer<T> whenCalculated) {
        if (needsUpdate.getAndSet(false)) {
            if (!requested.getAndSet(true)) {
                computer.accept(this);
            } else {
                // TODO: needsUpdate will need to be set back to true??
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
            // When this lambda is called, ret should be computed some time in the future
            /*self.requestForUpdate(); // requests value if not there, otherwise request for an update
            self.whenUpdated(value -> {
                consumer.accept(computer, value);
            });*/
            self.requestForUpdate(value -> {
                consumer.accept(computer, value);
            });
        });
        this.dependers.add(ret);
        return ret;
    }

    public static <T, U> BetterComputable<U> all(List<BetterComputable<T>> computables, Consumer<BetterComputable<U>> computer) {
        BetterComputable<U> ret = new BetterComputable<>(resultComputable -> {
            // TODO: optimize so you don't do this on every request?
            AtomicInteger counter = new AtomicInteger();
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
        for (BetterComputable<T> computable : computables) {
            computable.dependers.add(ret);
        }
        return ret;
    }
}
