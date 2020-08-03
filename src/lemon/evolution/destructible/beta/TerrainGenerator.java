package lemon.evolution.destructible.beta;

import lemon.engine.math.Vector3D;
import lemon.evolution.pool.VectorPool;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicInteger;

public class TerrainGenerator {
	private ExecutorService pool;
	private ScalarField<Vector3D> scalarField;
	private AtomicInteger queueSize;
	public TerrainGenerator(ExecutorService pool, ScalarField<Vector3D> scalarField) {
		this.pool = pool;
		this.scalarField = scalarField;
		this.queueSize = new AtomicInteger();
	}
	public void queueChunk(TerrainChunk chunk) {
		queueSize.incrementAndGet();
		pool.submit(() -> {
			generate(chunk);
			queueSize.decrementAndGet();
		});
	}
	public int getQueueSize() {
		return queueSize.get();
	}
	private void generate(TerrainChunk chunk) {
		try (var temp = VectorPool.ofEmpty()) {
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
		}
		chunk.setGenerated(true);
	}
}
