package lemon.evolution.world;

import lemon.engine.event.EventWith2;
import lemon.engine.game.Player;
import lemon.engine.math.Vector3D;
import lemon.engine.toolbox.Disposable;
import lemon.engine.toolbox.Disposables;
import lemon.evolution.destructible.beta.Terrain;
import lemon.evolution.physics.beta.CollisionContext;
import lemon.futility.FBufferedSetWithEvents;

import java.util.function.BiConsumer;

public class World implements Disposable {
	private static final Vector3D GRAVITY_VECTOR = Vector3D.of(0, -0.07f, 0);
	private static final float AIR_FRICTION = -0.02f;
	private final Disposables disposables = new Disposables();
	private final Terrain terrain;
	private final CollisionContext collisionContext;
	private final FBufferedSetWithEvents<Entity> entities = new FBufferedSetWithEvents<>();
	private final EventWith2<Vector3D, Float> onExplosion = new EventWith2<>();

	public World(Terrain terrain, CollisionContext collisionContext) {
		this.terrain = terrain;
		this.collisionContext = collisionContext;
	}

	public void generateExplosion(Vector3D position, float radius) {
		terrain.generateExplosion(position, radius);
		entities.forEach(entity -> {
			if (entity instanceof Player player) {
				float strength = Math.min(radius / 3f, 3f * radius / entity.position().distanceSquared(position));
				var direction = entity.position().subtract(position);
				if (direction.equals(Vector3D.ZERO)) {
					direction = Vector3D.ofRandomUnitVector();
				}
				entity.mutableVelocity().add(direction.scaleToLength(strength));
				player.health().setValue(player.health().getValue() - strength * 20f);
			}
		});
		onExplosion.callListeners(position, radius);
	}

	public void update(float dt) {
		entities.forEach(entity -> {
			entity.onUpdate().callListeners();
			collisionContext.collideWithWorld(
					entity.mutablePosition(),
					entity.mutableVelocity(),
					entity.mutableForce(),
					entity.scalar(),
					dt,
					entity.onCollide()::callListeners,
					entity::getCollisionResponse
			);
			entity.mutableForce().set(entity.getEnvironmentalForce());
		});
		entities.flush();
	}

	public Vector3D getEnvironmentalForce(Entity entity) {
		return GRAVITY_VECTOR.add(entity.velocity().multiply(AIR_FRICTION));
	}

	public Terrain terrain() {
		return terrain;
	}

	public FBufferedSetWithEvents<Entity> entities() {
		return entities;
	}

	@Override
	public void dispose() {
		disposables.dispose();
	}

	public EventWith2<Vector3D, Float> onExplosion() {
		return onExplosion;
	}

	public Disposable onExplosion(BiConsumer<Vector3D, Float> listener) {
		return onExplosion.add(listener);
	}
}
