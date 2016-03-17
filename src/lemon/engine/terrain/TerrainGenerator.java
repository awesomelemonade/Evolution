package lemon.engine.terrain;

import lemon.engine.function.MurmurHash;
import lemon.engine.function.PerlinNoise2D;
import lemon.engine.function.SzudzikPair;

public class TerrainGenerator {
	private PersistenceFunction biomeNoise;
	private PerlinNoise2D noise;
	
	public TerrainGenerator(int seed){
		biomeNoise = new PersistenceFunction(new PerlinNoise2D(MurmurHash.createWithSeed(seed-6), new SzudzikPair(), 0f, 1));
		noise = new PerlinNoise2D(MurmurHash.createWithSeed(seed), new SzudzikPair(), biomeNoise, 6);
	}
	public float generate(float x, float y){
		return noise.resolve(x/80f, y/80f)*20f;
	}
}
