package lemon.engine.terrain;

public class MurmurHash implements HashFunction {
	private int seed;
	private static final int c1 = 0xcc9e2d51;
	private static final int c2 = 0x1b873593;
	public MurmurHash(int seed){
		this.seed = seed;
	}
	@Override
	public int hash(int value) {
		int k1 = mixK1(value);
		int h1 = mixH1(seed, k1);
		return fmix(h1, 32);
	}
	private static int mixK1(int k1){
		k1 *= c1;
		k1 = Integer.rotateLeft(k1, 15);
		k1 *= c2;
		return k1;
	}
	private static int mixH1(int h1, int k1){
		h1 ^= k1;
		h1 = Integer.rotateLeft(h1, 13);
		h1 = h1 * 5 + 0xe6546b64;
		return h1;
	}
	private static int fmix(int h1, int length){
		h1 ^= length;
		h1 ^= h1 >>> 16;
		h1 *= 0x85ebca6b;
		h1 ^= h1 >>> 13;
		h1 *= 0xc2b2ae35;
		h1 ^= h1 >>> 16;
		return h1;
	}
	@Override
	public void setSeed(int seed) {
		this.seed = seed;
	}
	@Override
	public int getSeed() {
		return seed;
	}
}
