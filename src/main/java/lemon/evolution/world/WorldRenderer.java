package lemon.evolution.world;

import lemon.engine.math.Vector3D;
import lemon.engine.toolbox.Disposable;
import lemon.engine.toolbox.Disposables;
import lemon.evolution.destructible.beta.TerrainRenderer;

public class WorldRenderer implements Disposable {
	private static final float RENDER_DISTANCE = 5f;
	private final Disposables disposables = new Disposables();
	private final TerrainRenderer terrainRenderer;
	private final EntityRenderer entityRenderer;

	public WorldRenderer(World world) {
		this.terrainRenderer = new TerrainRenderer(world.terrain(), RENDER_DISTANCE);
		this.entityRenderer = disposables.add(new EntityRenderer(world.entities()));
	}

	public void render(Vector3D position) {
		terrainRenderer.render(position);
		entityRenderer.render();
	}

	public TerrainRenderer terrainRenderer() {
		return terrainRenderer;
	}

	public EntityRenderer entityRenderer() {
		return entityRenderer;
	}

	@Override
	public void dispose() {
		disposables.dispose();
	}
}
