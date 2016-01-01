package lemon.engine.terrain;

public class TerrainGenerator {
	private PerlinNoise noise;
	private static final PairingFunction pairer = new SzudzikPair();
	
	public TerrainGenerator(int seed){
		noise = new PerlinNoise(new MurmurHash(seed), pairer, (float)(1.0/Math.sqrt(2)), 6);
	}
	public float generate(float x, float y){
		return noise.noise((x/500f));
	}
}
