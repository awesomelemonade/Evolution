package lemon.evolution.destructible.beta;

import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class TerrainRendererTest {
	private static final List<Float> radii = List.of(0f, 1.5f, 2f, 3.14f, 5f, 10f, 20f);
	@Test
	public void checkForDuplicates() {
		TerrainRenderer renderer = new TerrainRenderer(null, 0f);
		for (float radius : radii) {
			renderer.setRenderDistance(radius);
			assertEquals(new HashSet<>(renderer.getTerrainOffsets()).size(), renderer.getTerrainOffsets().size());
		}
	}
	@Test
	public void checkForCompletion() {
		TerrainRenderer renderer = new TerrainRenderer(null, 0f);
		for (float radius : radii) {
			renderer.setRenderDistance(radius);
			var set = new HashSet<>(renderer.getTerrainOffsets());
			int ceil = (int) Math.ceil(radius);
			for (int i = -ceil; i <= ceil; i++) {
				for (int j = -ceil; j <= ceil; j++) {
					for (int k = -ceil; k <= ceil; k++) {
						if (i * i + k * k <= radius * radius) {
							int finalI = i;
							int finalJ = j;
							int finalK = k;
							assertTrue(set.contains(new TerrainRenderer.TerrainOffset(i, j, k)), () -> String.format("radius=%f [%d, %d, %d]", radius, finalI, finalJ, finalK));
						}
					}
				}
			}
		}
	}
}