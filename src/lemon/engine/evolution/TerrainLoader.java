package lemon.engine.evolution;

import java.util.logging.Level;
import java.util.logging.Logger;

import lemon.engine.control.Loader;
import lemon.engine.math.Percentage;
import lemon.engine.terrain.TerrainGenerator;

public class TerrainLoader implements Loader {
	private static final Logger logger = Logger.getLogger(TerrainLoader.class.getName());
	private TerrainGenerator terrainGenerator;
	private float[][] terrain;
	private Percentage percentage;
	public TerrainLoader(TerrainGenerator terrainGenerator, int width, int height){
		this.terrainGenerator = terrainGenerator;
		this.terrain = new float[width][height];
		this.percentage = new Percentage(width*height);
	}
	@Override
	public void load() {
		final int THREADS = 4;
		
		final boolean[] threads = new boolean[THREADS];
		
		percentage.setWhole(threads.length);
		
		final int size = terrain.length/(threads.length);
		
		long time = System.nanoTime();
		
		for(int i=0;i<threads.length;++i){
			final int index = i;
			new Thread(new Runnable(){
				@Override
				public void run() {
					for(int j=index*size;j<index*size+size;j++){
						for(int k=0;k<terrain[0].length;++k){
							terrain[j][k] = terrainGenerator.generate(j, k);
						}
					}
					threads[index] = true;
					percentage.setPart(percentage.getPart()+1);
				}
			}).start();
		}
		logger.log(Level.INFO, "Completed in "+((System.nanoTime()-time)/1000000000.0)+"s");
	}
	@Override
	public Percentage getPercentage() {
		return percentage;
	}
	public float[][] getTerrain(){
		return terrain;
	}
}
