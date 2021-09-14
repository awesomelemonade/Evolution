package lemon.evolution.world;

import lemon.engine.math.MathUtil;
import lemon.engine.math.Vector3D;
import lemon.engine.render.Renderable;
import lemon.evolution.destructible.beta.Terrain;
import lemon.evolution.physics.beta.CollisionContext;

import java.util.HashSet;
import java.util.Set;

public class World {
	private static final float AIR_RESISTANCE = 0.98f;
	private static final Vector3D GRAVITY_VECTOR = Vector3D.of(0, -0.005f, 0);
	private final Terrain terrain;
	private final CollisionContext collisionContext;
	private final Set<Renderable> renderables = new HashSet<>();
	private final Set<Entity> entities = new HashSet<>();

	public World(Terrain terrain, CollisionContext collisionContext) {
		this.terrain = terrain;
		this.collisionContext = collisionContext;
	}

	public void addEntity(Entity entity) {
		entities.add(entity);
		if (entity instanceof Renderable renderable) {
			renderables.add(renderable);
		}
	}

	public void removeEntity(Entity entity) {
		entities.remove(entity);
		if (entity instanceof Renderable renderable) {
			renderables.add(renderable);
		}
	}

	public void update(float dt) {
		for (Entity entity : entities) {
			entity.mutableVelocity().multiply(MathUtil.pow(AIR_RESISTANCE, dt));
			collisionContext.collideAndSlide(entity.mutablePosition(), entity.mutableVelocity(), GRAVITY_VECTOR, dt);
		}
	}

	public Terrain terrain() {
		return terrain;
	}

	public Set<Renderable> renderables() {
		return renderables;
	}
}
