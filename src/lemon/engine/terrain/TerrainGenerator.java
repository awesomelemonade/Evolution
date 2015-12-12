package lemon.engine.terrain;

import lemon.engine.game.OpenSimplexNoise;

public class TerrainGenerator {
	//OpenSimplexNoise noise = new OpenSimplexNoise(new Random().nextLong());
	//OpenSimplexNoise noise2 = new OpenSimplexNoise(new Random().nextLong());
	
	private final float TILE_SIZE;
	OpenSimplexNoise noise = new OpenSimplexNoise(0);
	OpenSimplexNoise noise2 = new OpenSimplexNoise(0);
	
	public TerrainGenerator(float TILE_SIZE){
		this.TILE_SIZE = TILE_SIZE;
	}
	
	public float generate(float x, float y){
		return Math.max((float)noise.eval(((double)x)/(12.0/TILE_SIZE), ((double)y)/(12.0/TILE_SIZE)), 0f)*3.2f+
				((float)noise2.eval(((double)x)/(30.0/TILE_SIZE), ((double)y)/(30.0/TILE_SIZE)))*8f;
	}
}
