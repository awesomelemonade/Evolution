package lemon.evolution.world;

import lemon.engine.event.EventWith2;
import lemon.engine.game.Player;
import lemon.engine.math.Vector3D;
import lemon.engine.toolbox.Disposable;
import lemon.engine.toolbox.Disposables;
import lemon.evolution.MapInfo;
import lemon.evolution.destructible.beta.Terrain;
import lemon.evolution.physics.beta.CollisionContext;
import lemon.futility.FBufferedSetWithEvents;
import lemon.futility.FilterableFSetWithEvents;

import java.util.Set;
import java.util.function.BiConsumer;

public class World implements Disposable {
	public static final Vector3D GRAVITY_VECTOR = Vector3D.of(0, -0.07f, 0);
	public static final float AIR_RESISTANCE = -0.02f;
	public static final float VOID_Y_COORDINATE = 0f;
	private final Disposables disposables = new Disposables();
	private final Terrain terrain;
	private final CollisionContext collisionContext;
	private final FBufferedSetWithEvents<Entity> entities = new FBufferedSetWithEvents<>();
	private final FilterableFSetWithEvents<Entity> filterableEntities = new FilterableFSetWithEvents<>(entities);
	private final EventWith2<Vector3D, Float> onExplosion = new EventWith2<>();
	private final MapInfo mapInfo;

	public World(Terrain terrain, CollisionContext collisionContext, MapInfo mapInfo) {
		this.terrain = terrain;
		this.collisionContext = collisionContext;
		this.mapInfo = mapInfo;
	}

	public void generateExplosion(Vector3D position, float radius) {
		terrain.generateExplosion(position, radius);
		players().forEach(player -> {
			float strength = Math.min(radius / 3f, 3f * radius / player.position().distanceSquared(position));
			var direction = player.position().subtract(position);
			if (direction.equals(Vector3D.ZERO)) {
				direction = Vector3D.ofRandomUnitVector();
			}
			player.mutableVelocity().add(direction.scaleToLength(strength));
			player.damage(strength * 20f);
		});
		onExplosion.callListeners(position, radius);
	}

	public void generateLineExplosion() {

	}

	public void generateExclusiveExplosion(Vector3D position, float radius, Player playerExcluded) {
		terrain.generateExplosion(position, radius);
		players().forEach(player -> {
			if (player != playerExcluded) {
				float strength = Math.min(radius / 3f, 3f * radius / player.position().distanceSquared(position));
				var direction = player.position().subtract(position);
				if (direction.equals(Vector3D.ZERO)) {
					direction = Vector3D.ofRandomUnitVector();
				}
				player.mutableVelocity().add(direction.scaleToLength(strength));
				player.damage(strength * 20f);
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
		entities.removeIf(entity -> entity.position().y() < VOID_Y_COORDINATE ||
				entity.position().toXZVector().lengthSquared() > mapInfo.worldRadius() * mapInfo.worldRadius());
		entities.flush();
	}

	public Vector3D getEnvironmentalForce(Entity entity) {
		return GRAVITY_VECTOR.add(entity.velocity().multiply(AIR_RESISTANCE));
	}

	public Terrain terrain() {
		return terrain;
	}

	public FBufferedSetWithEvents<Entity> entities() {
		return entities;
	}

	public FilterableFSetWithEvents<Entity> filterableEntities() {
		return filterableEntities;
	}

	public Set<Player> players() {
		return filterableEntities.ofFiltered(Player.class);
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
