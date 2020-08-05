package lemon.evolution.destructible.beta;

import lemon.engine.draw.Drawable;
import lemon.engine.draw.DynamicIndexedDrawable;
import lemon.engine.function.AbsoluteIntValue;
import lemon.engine.function.SzudzikIntPair;
import lemon.engine.math.Matrix;
import lemon.engine.math.Vector3D;
import lemon.evolution.Game;
import lemon.evolution.pool.VectorPool;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class Terrain {
	private Map<Long, TerrainChunk> chunks;
	private Consumer<TerrainChunk> generator;
	private Consumer<TerrainChunk> constructor;
	private Vector3D scalar;
	public Terrain(Consumer<TerrainChunk> generator, Vector3D scalar) {
		this(generator, (chunk) -> {
			chunk.setQueuedForConstruction(false);
			chunk.generateModel();
			chunk.setQueuedForUpdate(true);
		}, scalar);
	}
	public Terrain(Consumer<TerrainChunk> generator, ExecutorService pool, Vector3D scalar) {
		this(generator, (chunk) -> {
			pool.execute(() -> {
				chunk.setQueuedForConstruction(false);
				chunk.generateModel();
				chunk.setQueuedForUpdate(true);
			});
		}, scalar);
	}
	public Terrain(Consumer<TerrainChunk> generator, Consumer<TerrainChunk> constructor, Vector3D scalar) {
		this.chunks = new ConcurrentHashMap<>();
		this.generator = generator;
		this.constructor = constructor;
		this.scalar = scalar;
	}
	public void preloadChunk(int chunkX, int chunkY, int chunkZ) {
		getChunk(chunkX, chunkY, chunkZ);
	}
	public TerrainChunk getChunk(int chunkX, int chunkY, int chunkZ) {
		return getChunk(chunkX, chunkY, chunkZ, hashChunkCoordinates(chunkX, chunkY, chunkZ));
	}
	public TerrainChunk getChunk(int chunkX, int chunkY, int chunkZ, long hashed) {
		// Cannot use computeIfAbsent due to recursive issues
		if (chunks.containsKey(hashed)) {
			return chunks.get(hashed);
		} else {
			int offsetX = chunkX * TerrainChunk.SIZE;
			int offsetY = chunkY * TerrainChunk.SIZE;
			int offsetZ = chunkZ * TerrainChunk.SIZE;
			int subTerrainSize = TerrainChunk.SIZE + 1;
			TerrainChunk chunk = new TerrainChunk(chunkX, chunkY, chunkZ,
					getSubTerrain(offsetX, offsetY, offsetZ,
							subTerrainSize, subTerrainSize, subTerrainSize),
					scalar);
			if (chunks.putIfAbsent(hashed, chunk) == null) {
				generator.accept(chunk);
				return chunk;
			} else {
				// Some other thread was faster - we wasted some memory
				return chunks.get(hashed);
			}
		}
	}
	public void drawOrQueue(int chunkX, int chunkY, int chunkZ, BiConsumer<Matrix, Drawable> drawer) {
		TerrainChunk chunk = getChunk(chunkX, chunkY, chunkZ);
		DynamicIndexedDrawable drawable = chunk.getDrawable();
		if (chunk.isQueuedForUpdate()) {
			chunk.setQueuedForUpdate(false);
			if (drawable == null) {
				drawable = chunk.getModel().map(DynamicIndexedDrawable::new);
				chunk.setDrawable(drawable);
			} else {
				chunk.getModel().use(drawable::setData);
			}
		}
		if (drawable == null) {
			updateChunk(chunk);
			if (chunk.getModel() != null) {
				chunk.setQueuedForUpdate(false);
				drawable = chunk.getModel().map(DynamicIndexedDrawable::new);
				chunk.setDrawable(drawable);
			}
		}
		if (drawable != null) {
			drawer.accept(chunk.getTransformationMatrix(), drawable);
		}
	}
	private BoundedScalarGrid3D getSubTerrain(int x, int y, int z, int sizeX, int sizeY, int sizeZ) {
		return BoundedScalarGrid3D.of((a, b, c) -> this.get(a + x, b + y, c + z), sizeX, sizeY, sizeZ);
	}
	public static float getPercentage(Vector3D lower, Vector3D upper, float resolution, Predicate<Vector3D> predicate) {
		float count = 0;
		float total = 0;
		try (var temp = VectorPool.ofEmpty()) {
			for (float x = lower.getX(); x <= upper.getX(); x += resolution) {
				for (float y = lower.getY(); y <= upper.getY(); y += resolution) {
					for (float z = lower.getZ(); z <= upper.getZ(); z += resolution) {
						temp.set(x, y, z);
						if (predicate.test(temp)) {
							count++;
						}
						total++;
					}
				}
			}
		}
		return count / total;
	}
	public void updateChunk(TerrainChunk chunk) {
		if ((!chunk.isQueuedForConstruction()) && (chunk.hasBeenConstructed() || allGenerated(chunk))) {
			chunk.setQueuedForConstruction(true);
			constructor.accept(chunk);
		}
	}
	public boolean allGenerated(TerrainChunk chunk) {
		boolean allGenerated = chunk.isGenerated();
		for (int i = 1; i < 8; i++) {
			if (!getChunk(chunk.getChunkX() + (i & 0b1),
					chunk.getChunkY() + ((i >> 1) & 0b1),
					chunk.getChunkZ() + ((i >> 2) & 0b1)).isGenerated()) {
				allGenerated = false;
			}
		}
		return allGenerated;
	}
	public void generateExplosion(Vector3D point, float radius) {
		int floorX = (int) Math.floor((point.getX() - radius) / scalar.getX());
		int ceilX = (int) Math.ceil((point.getX() + radius) / scalar.getX());
		int floorY = (int) Math.floor((point.getY() - radius) / scalar.getY());
		int ceilY = (int) Math.ceil((point.getY() + radius) / scalar.getY());
		int floorZ = (int) Math.floor((point.getZ() - radius) / scalar.getZ());
		int ceilZ = (int) Math.ceil((point.getZ() + radius) / scalar.getZ());
		int floorChunkX = Math.floorDiv(floorX, TerrainChunk.SIZE);
		int ceilChunkX = Math.floorDiv(ceilX, TerrainChunk.SIZE);
		int floorChunkY = Math.floorDiv(floorY, TerrainChunk.SIZE);
		int ceilChunkY = Math.floorDiv(ceilY, TerrainChunk.SIZE);
		int floorChunkZ = Math.floorDiv(floorZ, TerrainChunk.SIZE);
		int ceilChunkZ = Math.floorDiv(ceilZ, TerrainChunk.SIZE);
		for (int i = floorChunkX; i <= ceilChunkX; i++) {
			for (int j = floorChunkY; j <= ceilChunkY; j++) {
				for (int k = floorChunkZ; k <= ceilChunkZ; k++) {
					generateExplosionInChunk(i, j, k, point, radius);
				}
			}
		}
	}
	public void generateExplosionInChunk(int chunkX, int chunkY, int chunkZ, Vector3D point, float radius) {
		TerrainChunk chunk = getChunk(chunkX, chunkY, chunkZ);
		int offsetX = chunk.getChunkX() * TerrainChunk.SIZE;
		int offsetY = chunk.getChunkY() * TerrainChunk.SIZE;
		int offsetZ = chunk.getChunkZ() * TerrainChunk.SIZE;
		try (var lower = VectorPool.ofEmpty();
			 var upper = VectorPool.ofEmpty()) {
			for (int i = 0; i < TerrainChunk.SIZE; i++) {
				for (int j = 0; j < TerrainChunk.SIZE; j++) {
					for (int k = 0; k < TerrainChunk.SIZE; k++) {
						lower.set(offsetX + i - 0.5f, offsetY + j - 0.5f, offsetZ + k - 0.5f);
						lower.multiply(scalar);
						upper.set(offsetX + i + 0.5f, offsetY + j + 0.5f, offsetZ + k + 0.5f);
						upper.multiply(scalar);
						float percentage = getPercentage(lower, upper, scalar.getX() / 4f, (v) -> {
							return v.getDistanceSquared(point) >= radius * radius;
						}) * 2f - 1f;
						if (percentage < 1f) {
							chunk.getData()[i][j][k] = Math.min(chunk.getData()[i][j][k], percentage);
						}
					}
				}
			}
		}
		updateChunk(chunk);
		for (int i = 1; i < 8; i++) {
			updateChunk(getChunk(chunkX - (i & 0b1), chunkY - ((i >> 1) & 0b1), chunkZ - ((i >> 2) & 0b1)));
		}
	}
	public float get(int x, int y, int z) {
		int chunkX = Math.floorDiv(x, TerrainChunk.SIZE);
		int chunkY = Math.floorDiv(y, TerrainChunk.SIZE);
		int chunkZ = Math.floorDiv(z, TerrainChunk.SIZE);
		int localX = Math.floorMod(x, TerrainChunk.SIZE);
		int localY = Math.floorMod(y, TerrainChunk.SIZE);
		int localZ = Math.floorMod(z, TerrainChunk.SIZE);
		long hashed = hashChunkCoordinates(chunkX, chunkY, chunkZ);
		TerrainChunk chunk = chunks.get(hashed);
		if (chunk == null) {
			throw new IllegalArgumentException(
					String.format("Chunk [(%d, %d, %d)=%d] has not been queued for generation",
							chunkX, chunkY, chunkZ, hashed));
		}
		if (!chunk.isGenerated()) {
			throw new IllegalArgumentException(
					String.format("Chunk [(%d, %d, %d)=%d] has not been generated",
							chunkX, chunkY, chunkZ, hashed));

		}
		return chunk.get(localX, localY, localZ);
	}
	private static long hashChunkCoordinates(int chunkX, int chunkY, int chunkZ) {
		chunkX = AbsoluteIntValue.HASHED.applyAsInt(chunkX);
		chunkY = AbsoluteIntValue.HASHED.applyAsInt(chunkY);
		chunkZ = AbsoluteIntValue.HASHED.applyAsInt(chunkZ);
		return SzudzikIntPair.pair(chunkX, chunkY, chunkZ);
	}
	public int getChunkX(float x) {
		return Math.floorDiv((int) Math.floor(x / scalar.getX()), TerrainChunk.SIZE);
	}
	public int getChunkY(float y) {
		return Math.floorDiv((int) Math.floor(y / scalar.getY()), TerrainChunk.SIZE);
	}
	public int getChunkZ(float z) {
		return Math.floorDiv((int) Math.floor(z / scalar.getZ()), TerrainChunk.SIZE);
	}
}
