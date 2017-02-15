package lemon.evolution;

import lemon.engine.control.Loader;
import lemon.engine.math.Percentage;
import lemon.engine.terrain.TerrainGenerator;
import lemon.engine.thread.ThreadManager;

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
		
		percentage.setWhole(terrain.length*terrain[0].length);
		
		final int size = terrain.length/(THREADS);
		
		for(int i=0;i<THREADS-1;++i){
			final int index = i;
			ThreadManager.INSTANCE.addThread(new Thread(new Runnable(){
				@Override
				public void run() {
					for(int j=index*size;j<index*size+size;j++){
						for(int k=0;k<terrain[0].length;++k){
							if(Thread.currentThread().isInterrupted()){
								return;
							}
							terrain[j][k] = terrainGenerator.generate(j, k);
							synchronized(percentage){
								percentage.setPart(percentage.getPart()+1);
							}
						}
					}
				}
			})).start();
		}
		ThreadManager.INSTANCE.addThread(new Thread(new Runnable(){
			@Override
			public void run() {
				for(int i=(THREADS-1)*size;i<terrain.length;++i){
					for(int j=0;j<terrain[0].length;++j){
						if(Thread.currentThread().isInterrupted()){
							return;
						}
						terrain[i][j] = terrainGenerator.generate(i, j);
						synchronized(percentage){
							percentage.setPart(percentage.getPart()+1);
						}
					}
				}
			}
		})).start();
	}
	@Override
	public Percentage getPercentage() {
		return percentage;
	}
	public float[][] getTerrain(){
		return terrain;
	}
}
