package lemon.evolution.world;

import lemon.engine.math.Vector3D;
import lemon.evolution.destructible.beta.Terrain;
import lemon.evolution.physics.beta.CollisionPacket;

import java.util.HashSet;
import java.util.Set;

public class World {
	private static final float DT_SCALE = 1f;
	private static final float FRICTION = 0.98f;
	private static final Vector3D GRAVITY_VECTOR = Vector3D.of(0, -0.005f, 0);
	private final Terrain terrain;
	private final Set<Entity> entities = new HashSet<>();

	public World(Terrain terrain) {
		this.terrain = terrain;
	}

	public void addEntity(Entity entity) {
		entities.add(entity);
	}

	public void removeEntity(Entity entity) {
		entities.remove(entity);
	}

	public void update(float dt) {
		for (Entity entity : entities) {
			entity.mutableVelocity().add(GRAVITY_VECTOR.multiply(DT_SCALE * dt));
			entity.mutableVelocity().multiply(FRICTION * DT_SCALE * dt);
			CollisionPacket.collideAndSlide(entity.mutablePosition(), entity.mutableVelocity(), entity.velocity().multiply(DT_SCALE * dt));
			entity.mutablePosition().add(entity.velocity().multiply(DT_SCALE * dt));
		}
	}

	public Terrain terrain() {
		return terrain;
	}
}
