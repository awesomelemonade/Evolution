package lemon.engine.function;

public class SzudzikIntPair {
	public static long pair(int x, int y) {
		if (x < 0 || y < 0) {
			throw new IllegalArgumentException(String.format("Out of Range: (%d, %d)", x, y));
		}
		long longX = x;
		long longY = y;
		return x >= y ? longX * longX + longX + longY : longX + longY * longY;
	}

	public static long pair(int x, int y, int z) {
		if (x < 0 || y < 0 || z < 0) {
			throw new IllegalArgumentException(String.format("Out of Range: (%d, %d, %d)", x, y, z));
		}
		long longX = x;
		long longY = y;
		long longZ = z;
		long maxXY = Math.max(longX, longY);
		long max = Math.max(maxXY, longZ);
		long hash = max * max * max + (2 * max * longZ) + longZ;
		if (max == longZ) {
			hash += maxXY * maxXY;
		}
		if (longY >= longX) {
			hash += longX + longY;
		} else {
			hash += longY;
		}
		return hash;
	}

	public static long pair(int x, int y, int z, int w) {
		return pair(Math.toIntExact(pair(x, y)), Math.toIntExact(pair(z, w)));
	}
}
