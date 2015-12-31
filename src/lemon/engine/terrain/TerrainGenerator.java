package lemon.engine.terrain;

public class TerrainGenerator {
	private HashFunction hasher;
	private static final PairingFunction pairer = new SzudzikPair();
	
	public TerrainGenerator(int seed){
		hasher = new MurmurHash(seed);
	}
	public float generate(float x, float y){
		return ((float)hasher.hash(pairer.pair((int)x, (int)y)))/((float)Integer.MAX_VALUE);
	}
}
