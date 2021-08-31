package lemon.engine.function;

import java.util.HashMap;
import java.util.Map;
import java.util.function.IntUnaryOperator;

public class MurmurHash implements IntUnaryOperator {
	private int seed;
	private static final int c1 = 0xcc9e2d51;
	private static final int c2 = 0x1b873593;
	private static Map<Integer, MurmurHash> murmurHashes;

	static {
		murmurHashes = new HashMap<>();
	}

	public static MurmurHash createWithSeed(int seed) {
		if (!murmurHashes.containsKey(seed)) {
			murmurHashes.put(seed, new MurmurHash(seed));
		}
		return murmurHashes.get(seed);
	}

	private MurmurHash(int seed) {
		this.seed = seed;
	}

	@Override
	public int applyAsInt(int operand) {
		int k1 = mixK1(operand);
		int h1 = mixH1(seed, k1);
		return fmix(h1, 32);
	}

	private static int mixK1(int k1) {
		k1 *= c1;
		k1 = Integer.rotateLeft(k1, 15);
		k1 *= c2;
		return k1;
	}

	private static int mixH1(int h1, int k1) {
		h1 ^= k1;
		h1 = Integer.rotateLeft(h1, 13);
		h1 = h1 * 5 + 0xe6546b64;
		return h1;
	}

	private static int fmix(int h1, int length) {
		h1 ^= length;
		h1 ^= h1 >>> 16;
		h1 *= 0x85ebca6b;
		h1 ^= h1 >>> 13;
		h1 *= 0xc2b2ae35;
		h1 ^= h1 >>> 16;
		return h1;
	}

	public void setSeed(int seed) {
		this.seed = seed;
	}

	public int getSeed() {
		return seed;
	}

	@Override
	public int hashCode() {
		return seed;
	}
}
