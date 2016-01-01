package lemon.engine.terrain;

public class TerrainGenerator {
	private PerlinNoise noise;
	
	public TerrainGenerator(int seed){
		noise = new PerlinNoise(MurmurHash.createWithSeed(seed), (float)(1.0/Math.sqrt(2)), 6);
	}
	public float generate(float x, float y){
		return noise.noise((x/500f));
	}
}
