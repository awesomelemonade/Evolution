package lemon.evolution.destructible.beta;

import lemon.engine.draw.DynamicIndexedDrawable;
import lemon.engine.function.AbsoluteIntValue;
import lemon.engine.function.SzudzikIntPair;
import lemon.engine.math.MathUtil;
import lemon.engine.math.Matrix;
import lemon.engine.math.Vector3D;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class Terrain {
	private Map<Long, TerrainChunk> chunks;
	private Map<Long, MarchingCube> marchingCubes;
	private Map<Long, Matrix> transformationMatrices;
	private Map<Long, DynamicIndexedDrawable> drawables;
	private Consumer<TerrainChunk> generator;
	private Vector3D scalar;
	public Terrain(Consumer<TerrainChunk> generator, Vector3D scalar) {
		this.chunks = new HashMap<>();
		this.marchingCubes = new HashMap<>();
		this.transformationMatrices = new HashMap<>();
		this.drawables = new HashMap<>();
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
		return drawables.computeIfAbsent(
				hashChunkCoordinates(chunkX, chunkY, chunkZ),
				(hashed) -> marchingCubes.get(hashed).getColoredModel().map(DynamicIndexedDrawable::new)
		);
	}
	private BoundedScalarGrid3D getSubTerrain(int x, int y, int z, int sizeX, int sizeY, int sizeZ) {
		return BoundedScalarGrid3D.of((a, b, c) -> this.get(a + x, b + y, c + z), sizeX, sizeY, sizeZ);
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
}
