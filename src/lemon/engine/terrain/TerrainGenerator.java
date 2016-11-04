package lemon.engine.terrain;

import lemon.engine.function.MurmurHash;
import lemon.engine.function.PerlinNoise2D;
import lemon.engine.function.SzudzikIntPair;

public class TerrainGenerator {
	private PersistenceFunction biomeNoise;
	private PerlinNoise2D noise;
	private static final float goldenRatio = (float)((1f+Math.sqrt(5))/2f);
	private static final float root2 = (float)(1f/Math.sqrt(2));
	
	public TerrainGenerator(int seed){
		biomeNoise = new PersistenceFunction(new PerlinNoise2D(MurmurHash.createWithSeed(seed-6), SzudzikIntPair.INSTANCE, 0f, 1), goldenRatio-root2-(root2/2f), root2);
		noise = new PerlinNoise2D(MurmurHash.createWithSeed(seed), SzudzikIntPair.INSTANCE, biomeNoise, 6);
	}
	public float generate(float x, float y){
		return noise.apply(x/80f, y/80f)*20f;
	}
}
