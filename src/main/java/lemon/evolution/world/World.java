package lemon.evolution.world;

import lemon.engine.math.Vector3D;
import lemon.engine.render.Renderable;
import lemon.engine.toolbox.Disposable;
import lemon.engine.toolbox.Disposables;
import lemon.evolution.destructible.beta.Terrain;
import lemon.evolution.physics.beta.CollisionContext;
import lemon.futility.FBufferedSetWithEvents;

import java.util.Set;

public class World implements Disposable {
	private static final Vector3D GRAVITY_VECTOR = Vector3D.of(0, -0.07f, 0);
	private static final float AIR_FRICTION = -0.02f;
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
			entity.onUpdate().callListeners();
			collisionContext.collideWithWorld(entity.mutablePosition(), entity.mutableVelocity(), entity.mutableForce(), dt, collision -> {
				entity.onCollide().callListeners(collision);
				return entity.getCollisionResponse();
			});
			entity.mutableForce().set(entity.getEnvironmentalForce());
		});
		entities.flush();
	}

	public Vector3D getEnvironmentalForce(Entity entity) {
		return GRAVITY_VECTOR.add(entity.velocity().multiply(AIR_FRICTION));
	}

	public CollisionContext collisionContext() {
		return collisionContext;
	}

	public Terrain terrain() {
		return terrain;
	}

	public FBufferedSetWithEvents<Entity> entities() {
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
