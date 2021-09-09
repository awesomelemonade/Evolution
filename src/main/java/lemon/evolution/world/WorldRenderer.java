package lemon.evolution.world;

import lemon.engine.math.Vector3D;
import lemon.evolution.destructible.beta.TerrainRenderer;

public class WorldRenderer {
	private final float RENDER_DISTANCE = 5f;
	private final World world;
	private final TerrainRenderer terrainRenderer;
	public WorldRenderer(World world) {
		this.world = world;
		this.terrainRenderer = new TerrainRenderer(world.terrain(), RENDER_DISTANCE);
	}

	public void render(Vector3D position) {
		terrainRenderer.render(position);
	}
}
