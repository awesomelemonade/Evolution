package lemon.engine.terrain;

public class TerrainGenerator {
	private PerlinNoise2D noise;
	
	public TerrainGenerator(int seed){
		noise = new PerlinNoise2D(MurmurHash.createWithSeed(seed), new SzudzikPair(), (float)(1.0/Math.sqrt(2)), 6);
	}
	public float generate(float x, float y){
		return noise.noise(x, y);
	}
}
