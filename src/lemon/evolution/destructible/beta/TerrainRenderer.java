package lemon.evolution.destructible.beta;

import lemon.engine.draw.Drawable;
import lemon.engine.math.Matrix;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.function.BiConsumer;

public class TerrainRenderer {
	private final Terrain terrain;
	private float renderDistance;
	private List<TerrainOffset> terrainOffsets = new ArrayList<>(); // Use Guava's immutable list

	public TerrainRenderer(Terrain terrain, float chunkDistance) {
		this.terrain = terrain;
		setRenderDistance(chunkDistance);
	}

	public void render(int chunkX, int chunkY, int chunkZ, BiConsumer<Matrix, Drawable> drawer) {
		terrain.flushForRendering();
		terrainOffsets.forEach(offset -> terrain.drawOrQueue(chunkX + offset.x, chunkY + offset.y, chunkZ + offset.z, drawer));
	}

	public void setRenderDistance(float chunkDistance) {
		this.renderDistance = chunkDistance;
		int ceil = (int) Math.ceil(chunkDistance);
		terrainOffsets = new ArrayList<>();
		for (int i = -ceil; i <= ceil; i++) {
			terrainOffsets.add(new TerrainOffset(0, i, 0));
		}
		for (int i = 0; i <= ceil; i++) {
			for (int k = 1; k <= ceil; k++) {
				if (i * i + k * k > chunkDistance * chunkDistance) {
					continue;
				}
				for (int j = -ceil; j <= ceil; j++) {
					terrainOffsets.add(new TerrainOffset(i, j, k));
					terrainOffsets.add(new TerrainOffset(k, j, -i));
					terrainOffsets.add(new TerrainOffset(-i, j, -k));
					terrainOffsets.add(new TerrainOffset(-k, j, i));
				}
			}
		}
		terrainOffsets.sort(Comparator.comparingInt(offset -> offset.x * offset.x + offset.y * offset.y + offset.z * offset.z));
	}
	public List<TerrainOffset> getTerrainOffsets() {
		return Collections.unmodifiableList(terrainOffsets);
	}
	public record TerrainOffset(int x, int y, int z) {}
}
