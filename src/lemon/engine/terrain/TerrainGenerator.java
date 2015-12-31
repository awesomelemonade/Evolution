package lemon.engine.terrain;

public class TerrainGenerator {
	private PerlinNoise noise;
	private static final PairingFunction pairer = new SzudzikPair();
	
	public TerrainGenerator(int seed){
		noise = new PerlinNoise(new MurmurHash(seed), pairer, 0.5f, 6);
	}
	public float generate(float x, float y){
		return noise.noise((x));
		//return ((float)hasher.hash(pairer.pair((int)x, (int)y)))/((float)Integer.MAX_VALUE);
	}
}
