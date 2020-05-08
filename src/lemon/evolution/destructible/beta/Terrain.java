package lemon.evolution.destructible.beta;

import lemon.engine.draw.DynamicIndexedDrawable;
import lemon.engine.function.AbsoluteIntValue;
import lemon.engine.function.SzudzikIntPair;
import lemon.engine.math.Vector3D;

import java.util.HashMap;
import java.util.Map;

public class Terrain {
	private ScalarField<Vector3D> generator;
	private Map<Long, TerrainChunk> chunks;
	private Map<Long, MarchingCube> marchingCubes;
	private Map<Long, DynamicIndexedDrawable> drawables;
	public Terrain(ScalarField<Vector3D> generator) {
		this.generator = generator;
		this.chunks = new HashMap<>();
		this.marchingCubes = new HashMap<>();
		this.drawables = new HashMap<>();
	}
	public void preloadChunk(int chunkX, int chunkY, int chunkZ) {
		long hashed = hashChunkCoordinates(chunkX, chunkY, chunkZ);
		if (!chunks.containsKey(hashed)) {
			chunks.put(hashed, generate(chunkX, chunkY, chunkZ));
		}
		if (!marchingCubes.containsKey(hashed)) {
			getMarchingCube(chunkX, chunkY, chunkZ);
		}
	}
	public DynamicIndexedDrawable getDrawableChunk(int chunkX, int chunkY, int chunkZ) {
		long hashed = hashChunkCoordinates(chunkX, chunkY, chunkZ);
		if (!drawables.containsKey(hashed)) {
			drawables.put(hashed, getMarchingCube(chunkX, chunkY, chunkZ)
					.getColoredModel().map(DynamicIndexedDrawable::new));
		}
		return drawables.get(hashed);
	}
	public MarchingCube getMarchingCube(int chunkX, int chunkY, int chunkZ) {
		long hashed = hashChunkCoordinates(chunkX, chunkY, chunkZ);
		int offsetX = chunkX * TerrainChunk.SIZE;
		int offsetY = chunkY * TerrainChunk.SIZE;
		int offsetZ = chunkZ * TerrainChunk.SIZE;
		int subTerrainSize = TerrainChunk.SIZE + 1;
		if (!marchingCubes.containsKey(hashed)) {
			MarchingCube marchingCube = new MarchingCube(
					getSubTerrain(offsetX, offsetY, offsetZ,
							subTerrainSize, subTerrainSize, subTerrainSize),
					new Vector3D(subTerrainSize, subTerrainSize, subTerrainSize),
					0f);
			marchingCubes.put(hashed, marchingCube);
		}
		return marchingCubes.get(hashed);
	}
	private BoundedScalarGrid3D getSubTerrain(int x, int y, int z, int sizeX, int sizeY, int sizeZ) {
		return BoundedScalarGrid3D.of((a, b, c) -> this.get(a + x, b + y, c + z), sizeX, sizeY, sizeZ);
	}
	private TerrainChunk generate(int chunkX, int chunkY, int chunkZ) {
		int offsetX = chunkX * TerrainChunk.SIZE;
		int offsetY = chunkY * TerrainChunk.SIZE;
		int offsetZ = chunkZ * TerrainChunk.SIZE;
		TerrainChunk chunk = new TerrainChunk();
		float[][][] data = chunk.getData();
		for (int i = 0; i < TerrainChunk.SIZE; i++) {
			for (int j = 0; j < TerrainChunk.SIZE; j++) {
				for (int k = 0; k < TerrainChunk.SIZE; k++) {
					data[i][j][k] = generator.get(
							new Vector3D(offsetX + i, offsetY + j, offsetZ + k));
				}
			}
		}
		return chunk;
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
			chunks.put(hashed, generate(chunkX, chunkY, chunkZ));
		}
		return chunks.get(hashed).get(localX, localY, localZ);
	}
	private static long hashChunkCoordinates(int chunkX, int chunkY, int chunkZ) {
		chunkX = AbsoluteIntValue.HASHED.applyAsInt(chunkX);
		chunkY = AbsoluteIntValue.HASHED.applyAsInt(chunkY);
		chunkZ = AbsoluteIntValue.HASHED.applyAsInt(chunkZ);
		return SzudzikIntPair.pair(chunkX, chunkY, chunkZ);
	}
}
