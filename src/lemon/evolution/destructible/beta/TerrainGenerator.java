package lemon.evolution.destructible.beta;

import lemon.engine.math.Vector3D;

import java.util.concurrent.ExecutorService;

public class TerrainGenerator {
	private final Object lock = new Object();
	private ExecutorService pool;
	private ScalarField<Vector3D> scalarField;
	private int queueSize;
	public TerrainGenerator(ExecutorService pool, ScalarField<Vector3D> scalarField) {
		this.pool = pool;
		this.scalarField = scalarField;
	}
	public void queueChunk(TerrainChunk chunk) {
		synchronized (lock) {
			queueSize++;
		}
		pool.submit(() -> {
			generate(chunk);
			synchronized (lock) {
				queueSize--;
			}
		});
	}
	public int getQueueSize() {
		synchronized (lock) {
			return queueSize;
		}
	}
	private void generate(TerrainChunk chunk) {
		Vector3D temp = new Vector3D();
		int offsetX = chunk.getChunkX() * TerrainChunk.SIZE;
		int offsetY = chunk.getChunkY() * TerrainChunk.SIZE;
		int offsetZ = chunk.getChunkZ() * TerrainChunk.SIZE;
		float[][][] data = chunk.getData();
		for (int i = 0; i < TerrainChunk.SIZE; i++) {
			for (int j = 0; j < TerrainChunk.SIZE; j++) {
				for (int k = 0; k < TerrainChunk.SIZE; k++) {
					temp.set(offsetX + i, offsetY + j, offsetZ + k);
					data[i][j][k] = scalarField.get(temp);
				}
			}
		}
		chunk.setGenerated(true);
	}
}
