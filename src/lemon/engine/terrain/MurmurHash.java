package lemon.engine.terrain;

public class MurmurHash implements HashFunction {
	private int seed;
	private static final int c1 = 0xcc9e2d51;
	private static final int c2 = 0x1b873593;
	@Override
	public int hash(int value) {
		int r1 = 15;
		int r2 = 13;
		int m = 5;
		int n = 0xe6546b64;
		int hash = seed;
		return 0;
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
