package lemon.evolution.world;

import lemon.engine.math.MathUtil;
import lemon.engine.math.Vector3D;
import lemon.engine.render.Renderable;
import lemon.engine.toolbox.Disposable;
import lemon.engine.toolbox.Disposables;
import lemon.evolution.destructible.beta.Terrain;
import lemon.evolution.physics.beta.CollisionContext;
import lemon.futility.FSetWithEvents;

import java.util.Set;

public class World implements Disposable {
	private static final float AIR_RESISTANCE = 0.98f;
	private static final Vector3D GRAVITY_VECTOR = Vector3D.of(0, -0.05f, 0);
	private final Disposables disposables = new Disposables();
	private final Terrain terrain;
	private final CollisionContext collisionContext;
	private final FSetWithEvents<Entity> entities = new FSetWithEvents<>();
	private final Set<Renderable> renderables = entities.ofFiltered(Renderable.class, disposables::add);

	public World(Terrain terrain, CollisionContext collisionContext) {
		this.terrain = terrain;
		this.collisionContext = collisionContext;
	}


	public void removeEntity(Entity entity) {
		entities.remove(entity);
		if (entity instanceof Renderable renderable) {
			renderables.remove(renderable);
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
