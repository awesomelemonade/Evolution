package lemon.evolution.destructible.beta;

import lemon.engine.draw.Drawable;
import lemon.engine.draw.DynamicIndexedDrawable;
import lemon.engine.function.AbsoluteIntValue;
import lemon.engine.function.SzudzikIntPair;
import lemon.engine.math.Matrix;
import lemon.engine.math.Vector3D;

import java.util.HashMap;
import java.util.Map;
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
			chunk.generateColoredModel();
			chunk.setQueuedForUpdate(true);
		}, scalar);
	}
	public Terrain(Consumer<TerrainChunk> generator, ExecutorService pool, Vector3D scalar) {
		this(generator, (chunk) -> {
			pool.submit(() -> {
				chunk.setQueuedForConstruction(false);
				chunk.generateColoredModel();
				chunk.setQueuedForUpdate(true);
			});
		}, scalar);
	}
	public Terrain(Consumer<TerrainChunk> generator, Consumer<TerrainChunk> constructor, Vector3D scalar) {
		this.chunks = new HashMap<>();
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
		return chunks.computeIfAbsent(hashed, x -> {
			int offsetX = chunkX * TerrainChunk.SIZE;
			int offsetY = chunkY * TerrainChunk.SIZE;
			int offsetZ = chunkZ * TerrainChunk.SIZE;
			int subTerrainSize = TerrainChunk.SIZE + 1;
			TerrainChunk newChunk = new TerrainChunk(chunkX, chunkY, chunkZ,
					getSubTerrain(offsetX, offsetY, offsetZ,
							subTerrainSize, subTerrainSize, subTerrainSize),
					new Vector3D(subTerrainSize, subTerrainSize, subTerrainSize),
					scalar);
			generator.accept(newChunk);
			return newChunk;
		});
	}
	public void drawOrQueue(int chunkX, int chunkY, int chunkZ, BiConsumer<Matrix, Drawable> drawer) {
		TerrainChunk chunk = getChunk(chunkX, chunkY, chunkZ);
		DynamicIndexedDrawable drawable = chunk.getDrawable();
		if (chunk.isQueuedForUpdate()) {
			chunk.setQueuedForUpdate(false);
			if (drawable == null) {
				drawable = chunk.getColoredModel().map(DynamicIndexedDrawable::new);
				chunk.setDrawable(drawable);
			} else {
				chunk.getColoredModel().use(drawable::setData);
			}
		}
		if (drawable == null) {
			boolean allGenerated = chunk.isGenerated();
			for (int i = 1; i < 8; i++) {
				if (!getChunk(chunkX + (i & 0b1),
						chunkY + ((i >> 1) & 0b1),
						chunkZ + ((i >> 2) & 0b1)).isGenerated()) {
					allGenerated = false;
				}
			}
			if (allGenerated) {
				updateChunk(chunk);
				if (chunk.getColoredModel() != null) {
					chunk.setQueuedForUpdate(false);
					drawable = chunk.getColoredModel().map(DynamicIndexedDrawable::new);
					chunk.setDrawable(drawable);
				}
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
		Vector3D temp = new Vector3D();
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
		return count / total;
	}
	public void updateChunk(TerrainChunk chunk) {
		if (!chunk.isQueuedForConstruction()) {
			chunk.setQueuedForConstruction(true);
			constructor.accept(chunk);
		}
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
					long hashed = hashChunkCoordinates(i, j, k);
					generateExplosionInChunk(hashed, point, radius);
				}
			}
		}
	}
	public void generateExplosionInChunk(long hashed, Vector3D point, float radius) {
		TerrainChunk chunk = chunks.get(hashed);
		Vector3D lower = new Vector3D();
		Vector3D upper = new Vector3D();
		int offsetX = chunk.getChunkX() * TerrainChunk.SIZE;
		int offsetY = chunk.getChunkY() * TerrainChunk.SIZE;
		int offsetZ = chunk.getChunkZ() * TerrainChunk.SIZE;
		for (int i = 0; i < TerrainChunk.SIZE; i++) {
			for (int j = 0; j < TerrainChunk.SIZE; j++) {
				for (int k = 0; k < TerrainChunk.SIZE; k++) {
					lower.set(offsetX + i - 0.5f, offsetY + j - 0.5f, offsetZ + k - 0.5f);
					lower.multiply(scalar);
					upper.set(offsetX + i + 0.5f, offsetY + j + 0.5f, offsetZ + k + 0.5f);
					upper.multiply(scalar);
					chunk.getData()[i][j][k] = Math.min(chunk.getData()[i][j][k],
							1f - getPercentage(lower, upper, scalar.getX() / 4f, (v) -> {
						return v.getDistanceSquared(point) <= radius * radius;
					}) * 2f);
				}
			}
		}
		updateChunk(chunk);
		for (int i = 1; i < 8; i++) {
			TerrainChunk neighborChunk = chunks.get(
					hashChunkCoordinates(chunk, -(i & 0b1), -((i >> 1) & 0b1), -((i >> 2) & 0b1)));
			// Could potentially be null (not preloaded)
			if (neighborChunk != null) {
				updateChunk(neighborChunk);
			}
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
		if (!chunks.containsKey(hashed)) {
			throw new IllegalArgumentException(
					String.format("Chunk [(%d, %d, %d)=%d] has not been queued for generation",
							chunkX, chunkY, chunkZ, hashed));
		}
		TerrainChunk chunk = chunks.get(hashed);
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
	private static long hashChunkCoordinates(TerrainChunk chunk, int offsetX, int offsetY, int offsetZ) {
		return hashChunkCoordinates(chunk.getChunkX() + offsetX, chunk.getChunkY() + offsetY, chunk.getChunkZ() + offsetZ);
	}
}
