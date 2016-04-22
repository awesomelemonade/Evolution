package lemon.engine.evolution;

import lemon.engine.control.Loader;
import lemon.engine.math.Percentage;
import lemon.engine.terrain.TerrainGenerator;

public class TerrainLoader implements Loader {
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
		
		percentage.setWhole(terrain.length*terrain[0].length);
		
		final int size = terrain.length/(threads.length);
		
		for(int i=0;i<threads.length-1;++i){
			final int index = i;
			new Thread(new Runnable(){
				@Override
				public void run() {
					for(int j=index*size;j<index*size+size;j++){
						for(int k=0;k<terrain[0].length;++k){
							terrain[j][k] = terrainGenerator.generate(j, k);
							synchronized(percentage){
								percentage.setPart(percentage.getPart()+1);
							}
						}
					}
					threads[index] = true;
				}
			}).start();
		}
		new Thread(new Runnable(){
			@Override
			public void run() {
				for(int i=(threads.length-1)*size;i<terrain.length;++i){
					for(int j=0;j<terrain[0].length;++j){
						terrain[i][j] = terrainGenerator.generate(i, j);
						synchronized(percentage){
							percentage.setPart(percentage.getPart()+1);
						}
					}
				}
			}
		}).start();
	}
	@Override
	public Percentage getPercentage() {
		return percentage;
	}
	public float[][] getTerrain(){
		return terrain;
	}
}
