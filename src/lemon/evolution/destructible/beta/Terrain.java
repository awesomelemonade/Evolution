package lemon.evolution.destructible.beta;

import lemon.engine.draw.Drawable;
import lemon.engine.function.AbsoluteIntValue;
import lemon.engine.function.SzudzikIntPair;
import lemon.engine.math.MathUtil;
import lemon.engine.math.Matrix;
import lemon.engine.math.Vector3D;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executor;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class Terrain {
	private final Map<Long, TerrainChunk> chunks;
	private final TerrainGenerator generator;
	private final Executor poolExecutor;
	private final Vector3D scalar;
	private final ConcurrentLinkedQueue<Runnable> updaters;

	public Terrain(TerrainGenerator generator, Executor poolExecutor, Vector3D scalar) {
		this.chunks = new ConcurrentHashMap<>();
		this.generator = generator;
		this.poolExecutor = poolExecutor;
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
		getChunk(chunkX, chunkY, chunkZ).drawable().request();
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
			return new TerrainChunk(this, chunkX, chunkY, chunkZ,
					getSubTerrain(offsetX, offsetY, offsetZ,
							subTerrainSize, subTerrainSize, subTerrainSize),
					generator, poolExecutor, updaters::add);
		});
	}

	public void drawOrQueue(int chunkX, int chunkY, int chunkZ, BiConsumer<Matrix, Drawable> drawer) {
		TerrainChunk chunk = getChunk(chunkX, chunkY, chunkZ);
		chunk.drawable().requestAndGetValue().ifPresent(
				drawable -> drawer.accept(chunk.getTransformationMatrix(), drawable));
	}

	private BoundedScalarGrid3D getSubTerrain(int x, int y, int z, int sizeX, int sizeY, int sizeZ) {
		return BoundedScalarGrid3D.of((a, b, c) -> this.get(a + x, b + y, c + z), sizeX, sizeY, sizeZ);
	}

	public static float getPercentage(Vector3D lower, Vector3D upper, float resolution, float radius) {
		float radiusSquared = radius * radius;
		float count = 0;
		float total = 0;
		for (float x = lower.x(); x <= upper.x(); x += resolution) {
			for (float y = lower.y(); y <= upper.y(); y += resolution) {
				for (float z = lower.z(); z <= upper.z(); z += resolution) {
					float lengthSquared = x * x + y * y + z * z;
					if (lengthSquared >= radiusSquared) {
						count++;
					}
					total++;
				}
			}
		}
		return count / total;
	}

	public void forEachChunk(Vector3D point, float radius, Consumer<TerrainChunk> chunk) {
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
					chunk.accept(getChunk(i, j, k));
				}
			}
		}
	}

	public void generateExplosion(Vector3D point, float radius) {
		forEachChunk(point, radius, chunk -> generateExplosionInChunk(chunk, point, radius));
	}

	public void terraform(Vector3D point, float radius, float dt, float brushSpeed) {
		forEachChunk(point, radius, chunk -> terraform(chunk, point, radius, dt, brushSpeed));
	}

	public float smoothstep(float min, float max, float t) {
		t = MathUtil.saturate((t - min) / (max - min));
		return t * t * (3 - 2 * t);
	}

	public void terraform(TerrainChunk chunk, Vector3D origin, float radius, float dt, float brushSpeed) {
		chunk.updateData(data -> {
			int offsetX = chunk.getChunkX() * TerrainChunk.SIZE;
			int offsetY = chunk.getChunkY() * TerrainChunk.SIZE;
			int offsetZ = chunk.getChunkZ() * TerrainChunk.SIZE;
			for (int i = 0; i < TerrainChunk.SIZE; i++) {
				for (int j = 0; j < TerrainChunk.SIZE; j++) {
					for (int k = 0; k < TerrainChunk.SIZE; k++) {
						var point = Vector3D.of(offsetX + i, offsetY + j, offsetZ + k).multiply(scalar);
						float distanceSquared = origin.distanceSquared(point);
						if (distanceSquared <= radius * radius) {
							float distance = (float) Math.sqrt(distanceSquared);
							float brushWeight = smoothstep(radius, radius * 0.7f, distance);
							data[i][j][k] += brushSpeed * brushWeight * dt;
						}
					}
				}
			}
		});
	}

	public void generateExplosionInChunk(TerrainChunk chunk, Vector3D point, float radius) {
		chunk.updateData(data -> {
			int offsetX = chunk.getChunkX() * TerrainChunk.SIZE;
			int offsetY = chunk.getChunkY() * TerrainChunk.SIZE;
			int offsetZ = chunk.getChunkZ() * TerrainChunk.SIZE;
			for (int i = 0; i < TerrainChunk.SIZE; i++) {
				for (int j = 0; j < TerrainChunk.SIZE; j++) {
					for (int k = 0; k < TerrainChunk.SIZE; k++) {
						var lower = Vector3D.of(offsetX + i - 0.5f, offsetY + j - 0.5f, offsetZ + k - 0.5f)
								.multiply(scalar).subtract(point);
						var upper = Vector3D.of(offsetX + i + 0.5f, offsetY + j + 0.5f, offsetZ + k + 0.5f)
								.multiply(scalar).subtract(point);
						float percentage = getPercentage(lower, upper, scalar.x() / 4f, radius) * 2f - 1f;
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

	public Vector3D scalar() {
		return scalar;
	}
}
