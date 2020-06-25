package lemon.evolution.destructible.beta;

import lemon.engine.draw.DynamicIndexedDrawable;
import lemon.engine.function.AbsoluteIntValue;
import lemon.engine.function.SzudzikIntPair;
import lemon.engine.math.MathUtil;
import lemon.engine.math.Matrix;
import lemon.engine.math.Vector3D;
import lemon.engine.toolbox.Color;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;

public class Terrain {
	private Map<Long, TerrainChunk> chunks;
	private Map<Long, MarchingCube> marchingCubes;
	private Map<Long, Matrix> transformationMatrices;
	private Map<Long, DynamicIndexedDrawable> drawables;
	private Map<Long, Color> colors;
	private Set<Long> queuedForUpdate;
	private Consumer<TerrainChunk> generator;
	private Vector3D scalar;
	public Terrain(Consumer<TerrainChunk> generator, Vector3D scalar) {
		this.chunks = new HashMap<>();
		this.marchingCubes = new HashMap<>();
		this.transformationMatrices = new HashMap<>();
		this.drawables = new HashMap<>();
		this.queuedForUpdate = new HashSet<>();
		this.colors = new HashMap<>();
		this.generator = generator;
		this.scalar = scalar;
	}
	public boolean preloadChunk(int chunkX, int chunkY, int chunkZ) {
		long hashed = hashChunkCoordinates(chunkX, chunkY, chunkZ);
		boolean loaded = true;
		if (chunks.containsKey(hashed)) {
			if (!chunks.get(hashed).isGenerated()) {
				loaded = false;
			}
		} else {
			TerrainChunk chunk = new TerrainChunk(chunkX, chunkY, chunkZ);
			chunks.put(hashed, chunk);
			generator.accept(chunk);
			loaded = false;
		}
		if (!marchingCubes.containsKey(hashed)) {
			int offsetX = chunkX * TerrainChunk.SIZE;
			int offsetY = chunkY * TerrainChunk.SIZE;
			int offsetZ = chunkZ * TerrainChunk.SIZE;
			int subTerrainSize = TerrainChunk.SIZE + 1;
			MarchingCube marchingCube = new MarchingCube(
					getSubTerrain(offsetX, offsetY, offsetZ,
							subTerrainSize, subTerrainSize, subTerrainSize),
					new Vector3D(subTerrainSize, subTerrainSize, subTerrainSize),
					0f);
			marchingCubes.put(hashed, marchingCube);
		}
		return loaded;
	}
	public boolean queueForDrawable(int chunkX, int chunkY, int chunkZ) {
		if (drawables.containsKey(hashChunkCoordinates(chunkX, chunkY, chunkZ))) {
			return true;
		}
		boolean loaded = true;
		for (int i = 0; i < 8; i++) {
			if (!preloadChunk(chunkX + (i & 0b1),
					chunkY + ((i >> 1) & 0b1),
					chunkZ + ((i >> 2) & 0b1))) {
				loaded = false;
			}
		}
		return loaded;
	}
	public Matrix getTransformationMatrix(int chunkX, int chunkY, int chunkZ) {
		return transformationMatrices.computeIfAbsent(
				hashChunkCoordinates(chunkX, chunkY, chunkZ), (x) -> {
					Vector3D translation = new Vector3D(
							scalar.getX() * chunkX * TerrainChunk.SIZE,
							scalar.getY() * chunkY * TerrainChunk.SIZE,
							scalar.getZ() * chunkZ * TerrainChunk.SIZE
					);
					return MathUtil.getTranslation(translation)
							.multiply(MathUtil.getScalar(scalar));
				});
	}
	public DynamicIndexedDrawable getDrawableChunk(int chunkX, int chunkY, int chunkZ) {
		long hashed = hashChunkCoordinates(chunkX, chunkY, chunkZ);
		Color color = colors.computeIfAbsent(hashed, (x) -> Color.randomOpaque());
		if (queuedForUpdate.contains(hashed)) {
			queuedForUpdate.remove(hashed);
			return drawables.compute(hashed, (key, value) -> {
				MarchingCube marchingCube = marchingCubes.get(hashed);
				if (value == null) {
					return marchingCube.getColoredModel(color).map(DynamicIndexedDrawable::new);
				} else {
					marchingCube.getColoredModel(color).use(value::setData);
					return value;
				}
			});
		} else {
			return drawables.computeIfAbsent(
					hashed,
					(x) -> marchingCubes.get(x).getColoredModel(color).map(DynamicIndexedDrawable::new)
			);
		}
	}
	private BoundedScalarGrid3D getSubTerrain(int x, int y, int z, int sizeX, int sizeY, int sizeZ) {
		return BoundedScalarGrid3D.of((a, b, c) -> this.get(a + x, b + y, c + z), sizeX, sizeY, sizeZ);
	}
	public static float getPercentage(Vector3D lower, Vector3D upper, float resolution, Predicate<Vector3D> predicate) {
		float count = 0;
		float total = 0;
		for (float x = lower.getX(); x <= upper.getX(); x += resolution) {
			for (float y = lower.getY(); y <= upper.getY(); y += resolution) {
				for (float z = lower.getZ(); z <= upper.getZ(); z += resolution) {
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
		queuedForUpdate.add(hashed);
		queuedForUpdate.add(hashChunkCoordinates(chunk, -1, 0, 0));
		queuedForUpdate.add(hashChunkCoordinates(chunk, 0, -1, 0));
		queuedForUpdate.add(hashChunkCoordinates(chunk, 0, 0, -1));
		queuedForUpdate.add(hashChunkCoordinates(chunk, -1, -1, 0));
		queuedForUpdate.add(hashChunkCoordinates(chunk, 0, -1, -1));
		queuedForUpdate.add(hashChunkCoordinates(chunk, -1, 0, -1));
		queuedForUpdate.add(hashChunkCoordinates(chunk, -1, -1, -1));
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
