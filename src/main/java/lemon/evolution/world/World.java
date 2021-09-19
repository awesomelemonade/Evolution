package lemon.evolution.world;

import lemon.engine.math.Vector3D;
import lemon.engine.render.Renderable;
import lemon.engine.toolbox.Disposable;
import lemon.engine.toolbox.Disposables;
import lemon.evolution.destructible.beta.Terrain;
import lemon.evolution.physics.beta.CollisionContext;
import lemon.futility.FBufferedSetWithEvents;
import lemon.futility.FSetWithEvents;

import java.util.Set;

public class World implements Disposable {
	private static final Vector3D GRAVITY_VECTOR = Vector3D.of(0, -0.05f, 0);
	private final Disposables disposables = new Disposables();
	private final Terrain terrain;
	private final CollisionContext collisionContext;
	private final FBufferedSetWithEvents<Entity> entities = new FBufferedSetWithEvents<>();
	private final Set<Renderable> renderables = entities.ofFiltered(Renderable.class, disposables::add);

	public World(Terrain terrain, CollisionContext collisionContext) {
		this.terrain = terrain;
		this.collisionContext = collisionContext;
	}

	public void update(float dt) {
		entities.forEach(entity -> {
			collisionContext.collideWithWorld(entity.mutablePosition(), entity.mutableVelocity(), entity.mutableForce(), dt, entity::onCollide);
			entity.mutableForce().set(entity.getEnvironmentalForce());
		});
		entities.flush();
	}

	public Vector3D getEnvironmentalForce() {
		return GRAVITY_VECTOR;
	}

	public CollisionContext collisionContext() {
		return collisionContext;
	}

	public Terrain terrain() {
		return terrain;
	}

	public FSetWithEvents<Entity> entities() {
		return entities;
	}

	public Set<Renderable> renderables() {
		return renderables;
	}

	@Override
	public void dispose() {
		disposables.dispose();
	}
}
