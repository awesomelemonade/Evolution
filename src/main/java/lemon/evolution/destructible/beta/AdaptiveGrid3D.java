package lemon.evolution.destructible.beta;

import java.util.function.Supplier;

public class AdaptiveGrid3D<T> {
    public static float DEFAULT_SWAP_RATIO = 0.1f;
    private SparseGrid3D<T> sparse;
    private T[][][] dense;
    private boolean useDense = false;
    private final int sizeA;
    private final int sizeB;
    private final int sizeC;
    private final Supplier<T> defaultSupplier;
    private final int swapThreshold;

    public AdaptiveGrid3D(int sizeA, int sizeB, int sizeC, Supplier<T> defaultSupplier) {
        this.sizeA = sizeA;
        this.sizeB = sizeB;
        this.sizeC = sizeC;
        this.defaultSupplier = defaultSupplier;
        this.sparse = new SparseGrid3D<>(sizeA, sizeB, sizeC, defaultSupplier);
        this.swapThreshold = (int) (sparse.capacity() * DEFAULT_SWAP_RATIO);
    }

    public T getOrDefault(int a, int b, int c, T defaultValue) {
        if (useDense) {
            return dense[a][b][c];
        } else {
            return sparse.getOrDefault(a, b, c, defaultValue);
        }
    }

    public T compute(int a, int b, int c) {
        if (useDense) {
            return dense[a][b][c];
        } else {
            var ret = sparse.compute(a, b, c);
            if (sparse.size() >= swapThreshold) {
                convertToDense();
            }
            return ret;
        }
    }

    @SuppressWarnings("unchecked")
    private void convertToDense() {
        this.dense = (T[][][]) new Object[sizeA][sizeB][sizeC];
        sparse.forEach((a, b, c, value) -> dense[a][b][c] = value);
        for (var a : dense) {
            for (var b : a) {
                for (int i = 0; i < b.length; i++) {
                    if (b[i] == null) {
                        b[i] = defaultSupplier.get();
                    }
                }
            }
        }
        sparse.clear();
        sparse = null;
        useDense = true;
    }
}
