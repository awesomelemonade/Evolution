package lemon.engine.function;

import java.util.function.IntBinaryOperator;

public enum SzudzikIntPair implements IntBinaryOperator {
	INSTANCE;
	@Override
	public int applyAsInt(int x, int y) {
		if (x < 0 || y < 0) {
			throw new IllegalArgumentException("Out of Range: " + x + " " + y);
		}
		long z = (x >= y ? x * x + x + y : x + y * y);
		if (z < Integer.MIN_VALUE || z > Integer.MAX_VALUE) {
			throw new IllegalArgumentException("Out of Range: " + z);
		}
		return (int) z;
	}
}
