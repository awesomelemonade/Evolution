package lemon.evolution.destructible.beta;

import lemon.engine.math.Vector3D;
import lemon.evolution.pool.VectorPool;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.LongAdder;

public class TerrainGenerator {
	private ExecutorService pool;
	private ScalarField<Vector3D> scalarField;
	private LongAdder queueSize;
	public TerrainGenerator(ExecutorService pool, ScalarField<Vector3D> scalarField) {
		this.pool = pool;
		this.scalarField = scalarField;
		this.queueSize = new LongAdder();
	}
	public void queueChunk(TerrainChunk chunk) {
		queueSize.increment();
		pool.execute(() -> {
			generate(chunk);
			queueSize.decrement();
		});
	}
	public int getQueueSize() {
		return queueSize.intValue();
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
