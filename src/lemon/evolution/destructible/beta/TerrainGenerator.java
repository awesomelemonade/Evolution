package lemon.evolution.destructible.beta;

import lemon.engine.math.Vector3D;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.LongAdder;
import java.util.function.Consumer;

public class TerrainGenerator {
	private ExecutorService pool;
	private ScalarField<Vector3D> scalarField;
	private LongAdder queueSize;

	public TerrainGenerator(ExecutorService pool, ScalarField<Vector3D> scalarField) {
		this.pool = pool;
		this.scalarField = scalarField;
		this.queueSize = new LongAdder();
	}

	public void queueChunk(TerrainChunk chunk, Consumer<float[][][]> consumer) {
		queueSize.increment();
		pool.execute(() -> {
			int offsetX = chunk.getChunkX() * TerrainChunk.SIZE;
			int offsetY = chunk.getChunkY() * TerrainChunk.SIZE;
			int offsetZ = chunk.getChunkZ() * TerrainChunk.SIZE;
			float[][][] data = new float[TerrainChunk.SIZE][TerrainChunk.SIZE][TerrainChunk.SIZE];
			for (int i = 0; i < TerrainChunk.SIZE; i++) {
				for (int j = 0; j < TerrainChunk.SIZE; j++) {
					for (int k = 0; k < TerrainChunk.SIZE; k++) {
						data[i][j][k] = scalarField.get(new Vector3D(offsetX + i, offsetY + j, offsetZ + k));
					}
				}
			}
			consumer.accept(data);
			queueSize.decrement();
		});
	}

	public int getQueueSize() {
		return queueSize.intValue();
	}
}
