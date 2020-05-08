package lemon.evolution.destructible.beta;

import lemon.engine.function.AbsoluteIntValue;
import lemon.engine.function.SzudzikIntPair;
import lemon.engine.math.Vector3D;

import java.util.Map;

public class Terrain {
	private ScalarField<Vector3D> generator;
	private Map<Long, TerrainChunk> chunks;
	public Terrain(ScalarField<Vector3D> generator) {
		this.generator = generator;
	}
	public BoundedScalarGrid3D getSubTerrain(int x, int y, int z, int sizeX, int sizeY, int sizeZ) {
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
		int chunkX = getChunkCoordinate(x);
		int chunkY = getChunkCoordinate(y);
		int chunkZ = getChunkCoordinate(z);
		int localX = Math.floorMod(x, TerrainChunk.SIZE);
		int localY = Math.floorMod(y, TerrainChunk.SIZE);
		int localZ = Math.floorMod(z, TerrainChunk.SIZE);
		long hashed = SzudzikIntPair.pair(chunkX, chunkY, chunkZ);
		if (!chunks.containsKey(hashed)) {
			chunks.put(hashed, generate(chunkX, chunkY, chunkZ));
		}
		return chunks.get(hashed).get(localX, localY, localZ);
	}
	public static int getChunkCoordinate(int coordinate) {
		return AbsoluteIntValue.HASHED.applyAsInt(
				Math.floorDiv(coordinate, TerrainChunk.SIZE));
	}
}
