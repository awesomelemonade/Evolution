package lemon.evolution.destructible.beta;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class SparseGrid3D<T> {
    private final Map<Long, T> map = new HashMap<>();
    private final long sizeA;
    private final long sizeB;
    private final long sizeC;
    private final Supplier<T> defaultSupplier;

    public SparseGrid3D(long sizeA, long sizeB, long sizeC, Supplier<T> defaultSupplier) {
        // Check out of bounds
        try {
            var ignored = Math.multiplyExact(Math.multiplyExact(sizeA, sizeB), sizeC);
        } catch (ArithmeticException e) {
            throw new IllegalStateException("Dimensions too large - overflow");
        }
        this.sizeA = sizeA;
        this.sizeB = sizeB;
        this.sizeC = sizeC;
        this.defaultSupplier = defaultSupplier;
    }

    public void set(int a, int b, int c, T value) {
        map.put(hash(a, b, c), value);
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
}
