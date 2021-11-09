package lemon.evolution.destructible.beta;

import lemon.engine.math.MathUtil;
import lemon.engine.math.Matrix;
import lemon.engine.math.Triangle;
import lemon.engine.math.Vector3D;
import lemon.engine.toolbox.TaskQueue;
import lemon.engine.draw.Drawable;
import lemon.engine.function.AbsoluteIntValue;
import lemon.engine.function.SzudzikIntPair;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class Terrain {
	private final Map<Long, TerrainChunk> chunks;
	private final TerrainGenerator generator;
	private final Executor poolExecutor;
	private final Vector3D scalar;
	private final TaskQueue updaters = TaskQueue.ofConcurrent();

	public Terrain(TerrainGenerator generator, Executor poolExecutor, Vector3D scalar) {
		this.chunks = new ConcurrentHashMap<>();
		this.generator = generator;
		this.poolExecutor = poolExecutor;
		this.scalar = scalar;
	}

	public void flushForRendering() {
		var time = System.nanoTime();
		updaters.run(() -> System.nanoTime() - time <= 10_000_000L);
	}

	public void preloadChunk(int chunkX, int chunkY, int chunkZ) {
		var chunk = getChunk(chunkX, chunkY, chunkZ);
		chunk.data().request();
		chunk.textureData().request();
	}

	public void preinitChunk(int chunkX, int chunkY, int chunkZ) {
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
					getSubTerrainTextureWeights(offsetX, offsetY, offsetZ,
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

	private BoundedGrid3D<float[]> getSubTerrainTextureWeights(int x, int y, int z, int sizeX, int sizeY, int sizeZ) {
		return BoundedGrid3D.of((a, b, c) -> this.getTextureWeights(a + x, b + y, c + z), sizeX, sizeY, sizeZ);
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
		terraform(point, radius, 1f, -100f, 0);
	}

	public void terraform(Vector3D point, float radius, float dt, float brushSpeed, int texture) {
		forEachChunk(point, radius, chunk -> terraform(chunk, point, radius, dt, brushSpeed, texture));
	}

	public float smoothstep(float min, float max, float t) {
		t = MathUtil.saturate((t - min) / (max - min));
		return t * t * (3 - 2 * t);
	}

	public void terraform(TerrainChunk chunk, Vector3D origin, float radius, float dt, float brushSpeed, int texture) {
		// Avoid making a copy of origin and using Vector for memory optimization
		var originX = origin.x();
		var originY = origin.y();
		var originZ = origin.z();
		var radiusSquared = radius * radius;
		int offsetX = chunk.getChunkX() * TerrainChunk.SIZE;
		int offsetY = chunk.getChunkY() * TerrainChunk.SIZE;
		int offsetZ = chunk.getChunkZ() * TerrainChunk.SIZE;
		chunk.updateAllData((data, textureData) -> {
			for (int i = 0; i < TerrainChunk.SIZE; i++) {
				for (int j = 0; j < TerrainChunk.SIZE; j++) {
					for (int k = 0; k < TerrainChunk.SIZE; k++) {
						var pointX = scalar.x() * (offsetX + i);
						var pointY = scalar.y() * (offsetY + j);
						var pointZ = scalar.z() * (offsetZ + k);
						var deltaX = originX - pointX;
						var deltaY = originY - pointY;
						var deltaZ = originZ - pointZ;
						var distanceSquared = deltaX * deltaX + deltaY * deltaY + deltaZ * deltaZ;
						if (distanceSquared <= radiusSquared) {
							float distance = (float) Math.sqrt(distanceSquared);
							float brushWeight = smoothstep(radius, radius * 0.7f, distance);
							var amount = brushSpeed * brushWeight * dt;
							data[i][j][k] += amount;
							var textureWeights = textureData.compute(i, j, k);
							textureWeights[texture] = Math.max(textureWeights[texture] + amount, 0);
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

	public float[] getTextureWeights(int x, int y, int z) {
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
		return chunk.getTextureWeights(localX, localY, localZ);
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

	public int getCollideX(float x) {
		return Math.floorDiv((int) Math.floor(x / scalar.x()), TerrainChunk.TRIANGLE_COORDS_TO_SUBDIVISION_COORDS);
	}

	public int getCollideY(float y) {
		return Math.floorDiv((int) Math.floor(y / scalar.y()), TerrainChunk.TRIANGLE_COORDS_TO_SUBDIVISION_COORDS);
	}

	public int getCollideZ(float z) {
		return Math.floorDiv((int) Math.floor(z / scalar.z()), TerrainChunk.TRIANGLE_COORDS_TO_SUBDIVISION_COORDS);
	}

	public List<Triangle> getTriangles(int collideX, int collideY, int collideZ) {
		var chunkX = Math.floorDiv(collideX, TerrainChunk.TRIANGLES_SUBDIVISION_SIZE);
		var chunkY = Math.floorDiv(collideY, TerrainChunk.TRIANGLES_SUBDIVISION_SIZE);
		var chunkZ = Math.floorDiv(collideZ, TerrainChunk.TRIANGLES_SUBDIVISION_SIZE);
		var collideXPart = Math.floorMod(collideX, TerrainChunk.TRIANGLES_SUBDIVISION_SIZE);
		var collideYPart = Math.floorMod(collideY, TerrainChunk.TRIANGLES_SUBDIVISION_SIZE);
		var collideZPart = Math.floorMod(collideZ, TerrainChunk.TRIANGLES_SUBDIVISION_SIZE);
		return getChunk(chunkX, chunkY, chunkZ).getTriangles(collideXPart, collideYPart, collideZPart);
	}

	public float getChunkDistance(float distance) {
		return distance / scalar.x() / TerrainChunk.SIZE;
	}

	public Vector3D scalar() {
		return scalar;
	}

	public int chunkCount() {
		return chunks.size();
	}
}
