package lemon.engine.terrain;

import lemon.engine.function.MurmurHash;
import lemon.engine.function.PerlinNoise2D;
import lemon.engine.function.SzudzikPair;

public class TerrainGenerator {
	private PerlinNoise2D noise;
	
	public TerrainGenerator(int seed){
		noise = new PerlinNoise2D(MurmurHash.createWithSeed(seed), new SzudzikPair(), (float)(1.0/Math.sqrt(2)), 6);
	}
	public float generate(float x, float y){
		return noise.resolve(x/20f, y/20f)*2f;
	}
}
