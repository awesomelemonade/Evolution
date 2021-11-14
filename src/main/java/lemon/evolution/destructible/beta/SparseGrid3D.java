package lemon.evolution.destructible.beta;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class SparseGrid3D<T> {
    private final Map<Long, T> map = new HashMap<>();
    private final long sizeA;
    private final long sizeB;
    private final long sizeC;
    private final int capacity;
    private final Supplier<T> defaultSupplier;

    public SparseGrid3D(long sizeA, long sizeB, long sizeC, Supplier<T> defaultSupplier) {
        // Check out of bounds
        try {
            this.capacity = Math.toIntExact(Math.multiplyExact(Math.multiplyExact(sizeA, sizeB), sizeC));
        } catch (ArithmeticException e) {
            throw new IllegalStateException("Dimensions too large - overflow");
        }
        this.sizeA = sizeA;
        this.sizeB = sizeB;
        this.sizeC = sizeC;
        this.defaultSupplier = defaultSupplier;
    }

    public T getOrDefault(int a, int b, int c, T defaultValue) {
        return map.getOrDefault(hash(a, b, c), defaultValue);
    }

    public T compute(int a, int b, int c) {
        return map.computeIfAbsent(hash(a, b, c), key -> defaultSupplier.get());
    }

    public long hash(int a, int b, int c) {
        return (a * sizeB + b) * sizeC + c;
    }

    public void forEach(DataConsumer<T> consumer) {
        for (var entry : map.entrySet()) {
            long hashed = entry.getKey();
            int c = (int) (hashed % sizeC);
            hashed /= sizeC;
            int b = (int) (hashed % sizeB);
            int a = (int) (hashed / sizeB);
            consumer.accept(a, b, c, entry.getValue());
        }
    }

    public void clear() {
        map.clear();
    }

    public int size() {
        return map.size();
    }

    public long capacity() {
        return capacity;
    }

    public interface DataConsumer<T> {
        public void accept(int a, int b, int c, T value);
    }
}
