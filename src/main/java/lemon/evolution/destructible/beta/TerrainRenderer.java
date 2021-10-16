package lemon.evolution.destructible.beta;

import com.google.common.collect.ImmutableList;
import lemon.engine.draw.Drawable;
import lemon.engine.math.Matrix;
import lemon.engine.math.Vector3D;
import lemon.engine.render.MatrixType;
import lemon.evolution.util.CommonPrograms3D;
import org.lwjgl.opengl.GL11;

import java.util.Comparator;
import java.util.function.BiConsumer;
import java.util.stream.Stream;

public class TerrainRenderer {
	private final Terrain terrain;
	private float renderDistance;
	private ImmutableList<TerrainOffset> terrainOffsets;

	public TerrainRenderer(Terrain terrain, float renderDistance) {
		this.terrain = terrain;
		setRenderDistance(renderDistance);
	}

	public void preload(Vector3D position) {
		preload(terrain.getChunkX(position.x()), terrain.getChunkY(position.y()), terrain.getChunkZ(position.z()));
	}

	public void preload(int chunkX, int chunkY, int chunkZ) {
		terrainOffsets.forEach(offset -> terrain.preloadChunk(chunkX + offset.x, chunkY + offset.y, chunkZ + offset.z));
	}

	public void render(Vector3D position) {
		terrain.flushForRendering();
		GL11.glEnable(GL11.GL_DEPTH_TEST);
		CommonPrograms3D.TERRAIN.use(program -> {
			draw(position, (matrix, drawable) -> {
				program.loadMatrix(MatrixType.MODEL_MATRIX, matrix);
				drawable.draw();
			});
		});
		GL11.glDisable(GL11.GL_DEPTH_TEST);
	}

	public void draw(Vector3D position, BiConsumer<Matrix, Drawable> drawer) {
		draw(terrain.getChunkX(position.x()), terrain.getChunkY(position.y()), terrain.getChunkZ(position.z()), drawer);
	}

	public void draw(int chunkX, int chunkY, int chunkZ, BiConsumer<Matrix, Drawable> drawer) {
		terrainOffsets.forEach(offset -> terrain.drawOrQueue(chunkX + offset.x, chunkY + offset.y, chunkZ + offset.z, drawer));
	}

	public void setRenderDistance(float chunkDistance) {
		this.renderDistance = chunkDistance;
		int ceil = (int) Math.ceil(chunkDistance);
		var builder = Stream.<TerrainOffset>builder();
		for (int i = -ceil; i <= ceil; i++) {
			builder.add(new TerrainOffset(0, i, 0));
		}
		for (int i = 0; i <= ceil; i++) {
			for (int k = 1; k <= ceil; k++) {
				if (i * i + k * k > chunkDistance * chunkDistance) {
					continue;
				}
				for (int j = -ceil; j <= ceil; j++) {
					builder.add(new TerrainOffset(i, j, k));
					builder.add(new TerrainOffset(k, j, -i));
					builder.add(new TerrainOffset(-i, j, -k));
					builder.add(new TerrainOffset(-k, j, i));
				}
			}
		}
		terrainOffsets = builder.build()
				.sorted(Comparator.comparingInt(offset -> offset.x * offset.x + offset.y * offset.y + offset.z * offset.z))
				.collect(ImmutableList.toImmutableList());
	}

	public ImmutableList<TerrainOffset> getTerrainOffsets() {
		return terrainOffsets;
	}

	public float getRenderDistance() {
		return renderDistance;
	}

	public record TerrainOffset(int x, int y, int z) {}
}
