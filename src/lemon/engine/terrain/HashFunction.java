package lemon.engine.terrain;

public interface HashFunction {
	public int hash(int value);
	public void setSeed(int seed);
	public int getSeed();
}
