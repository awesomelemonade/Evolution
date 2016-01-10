package lemon.engine.function;

public interface HashFunction extends Function<Integer, Integer> {
	public void setSeed(int seed);
	public int getSeed();
}
