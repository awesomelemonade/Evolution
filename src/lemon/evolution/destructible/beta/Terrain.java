package lemon.evolution.destructible.beta;

import lemon.engine.draw.Drawable;
import lemon.engine.function.AbsoluteIntValue;
import lemon.engine.function.SzudzikIntPair;
import lemon.engine.math.Matrix;
import lemon.engine.math.Vector3D;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.BiConsumer;
import java.util.function.Predicate;

public class Terrain {
	private final Map<Long, TerrainChunk> chunks;
	private final TerrainGenerator generator;
	private final Vector3D scalar;
	private final ConcurrentLinkedQueue<Runnable> updaters;

	public Terrain(TerrainGenerator generator, Vector3D scalar) {
		this.chunks = new ConcurrentHashMap<>();
		this.generator = generator;
		this.scalar = scalar;
		this.updaters = new ConcurrentLinkedQueue<>();
	}

	public void flushForRendering() {
		Runnable current;
		while ((current = updaters.poll()) != null) {
			current.run();
		}
	}

	public void preloadChunk(int chunkX, int chunkY, int chunkZ) {
		getChunk(chunkX, chunkY, chunkZ).getDrawable().request();
	}

	public TerrainChunk getChunk(int chunkX, int chunkY, int chunkZ) {
		return getChunk(chunkX, chunkY, chunkZ, hashChunkCoordinates(chunkX, chunkY, chunkZ));
	}

	public TerrainChunk getChunk(int chunkX, int chunkY, int chunkZ, long hashed) {
		return chunks.computeIfAbsent(hashed, currentHashed -> {
			int offsetX = chunkX * TerrainChunk.SIZE;
			int offsetY = chunkY * TerrainChunk.SIZE;
			int offsetZ = chunkZ * TerrainChunk.SIZE;
			int subTerrainSize = TerrainChunk.SIZE + 1;
			return new TerrainChunk(chunkX, chunkY, chunkZ,
					getSubTerrain(offsetX, offsetY, offsetZ,
							subTerrainSize, subTerrainSize, subTerrainSize),
					scalar, generator, updaters::add, this);
		});
	}

	public void drawOrQueue(int chunkX, int chunkY, int chunkZ, BiConsumer<Matrix, Drawable> drawer) {
		TerrainChunk chunk = getChunk(chunkX, chunkY, chunkZ);
		chunk.getDrawable().requestAndGetValue().ifPresent(
				drawable -> drawer.accept(chunk.getTransformationMatrix(), drawable));
	}

	private BoundedScalarGrid3D getSubTerrain(int x, int y, int z, int sizeX, int sizeY, int sizeZ) {
		return BoundedScalarGrid3D.of((a, b, c) -> this.get(a + x, b + y, c + z), sizeX, sizeY, sizeZ);
	}

	public static float getPercentage(Vector3D lower, Vector3D upper, float resolution, Predicate<Vector3D> predicate) {
		float count = 0;
		float total = 0;
		for (float x = lower.x(); x <= upper.x(); x += resolution) {
			for (float y = lower.y(); y <= upper.y(); y += resolution) {
				for (float z = lower.z(); z <= upper.z(); z += resolution) {
					if (predicate.test(new Vector3D(x, y, z))) {
						count++;
					}
					total++;
				}
			}
		}
		return count / total;
	}

	public void generateExplosion(Vector3D point, float radius) {
		int floorX = (int) Math.floor((point.x() - radius) / scalar.x());
		int ceilX = (int) Math.ceil((point.x() + radius) / scalar.x());
		int floorY = (int) Math.floor((point.y() - radius) / scalar.y());
		int ceilY = (int) Math.ceil((point.y() + radius) / scalar.y());
		int floorZ = (int) Math.floor((point.z() - radius) / scalar.z());
		int ceilZ = (int) Math.ceil((point.z() + radius) / scalar.z());
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
		chunk.updateData(data -> {
			int offsetX = chunk.getChunkX() * TerrainChunk.SIZE;
			int offsetY = chunk.getChunkY() * TerrainChunk.SIZE;
			int offsetZ = chunk.getChunkZ() * TerrainChunk.SIZE;
			for (int i = 0; i < TerrainChunk.SIZE; i++) {
				for (int j = 0; j < TerrainChunk.SIZE; j++) {
					for (int k = 0; k < TerrainChunk.SIZE; k++) {
						var lower = new Vector3D(offsetX + i - 0.5f, offsetY + j - 0.5f, offsetZ + k - 0.5f)
								.multiply(scalar);
						var upper = new Vector3D(offsetX + i + 0.5f, offsetY + j + 0.5f, offsetZ + k + 0.5f)
								.multiply(scalar);
						float percentage = getPercentage(lower, upper, scalar.x() / 4f, (v) -> {
							return v.distanceSquared(point) >= radius * radius;
						}) * 2f - 1f;
						if (percentage < 1f) {
							data[i][j][k] = Math.min(data[i][j][k], percentage);
						}
					}
				}
			}
		});
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
		return chunk.get(localX, localY, localZ);
	}

	private static long hashChunkCoordinates(int chunkX, int chunkY, int chunkZ) {
		chunkX = AbsoluteIntValue.HASHED.applyAsInt(chunkX);
		chunkY = AbsoluteIntValue.HASHED.applyAsInt(chunkY);
		chunkZ = AbsoluteIntValue.HASHED.applyAsInt(chunkZ);
		return SzudzikIntPair.pair(chunkX, chunkY, chunkZ);
	}

	public int getChunkX(float x) {
		return Math.floorDiv((int) Math.floor(x / scalar.x()), TerrainChunk.SIZE);
	}

	public int getChunkY(float y) {
		return Math.floorDiv((int) Math.floor(y / scalar.y()), TerrainChunk.SIZE);
	}

	public int getChunkZ(float z) {
		return Math.floorDiv((int) Math.floor(z / scalar.z()), TerrainChunk.SIZE);
	}
}
