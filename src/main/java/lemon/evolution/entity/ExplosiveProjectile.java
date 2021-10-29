package lemon.evolution.entity;

import lemon.engine.math.Vector3D;
import lemon.engine.toolbox.Disposable;
import lemon.engine.toolbox.Disposables;
import lemon.evolution.physics.beta.CollisionResponse;
import lemon.evolution.world.AbstractEntity;
import lemon.evolution.world.Location;

public class ExplosiveProjectile extends AbstractEntity implements Disposable {
	private final Disposables disposables = new Disposables();

	public ExplosiveProjectile(Location location, Vector3D velocity, ExplosiveProjectileType type) {
		super(location, velocity, type.scalar());
		this.meta().set("type", type);
		this.disposables.add(this.onCollide().add(explosionPosition -> {
			removeFromWorld();
			world().terrain().generateExplosion(explosionPosition, 3f);
			world().entities().forEach(entity -> {
				if (!(entity instanceof ExplosiveProjectile)) {
					float strength = Math.min(2f, 10f / entity.position().distanceSquared(explosionPosition));
					var direction = entity.position().subtract(explosionPosition);
					if (direction.equals(Vector3D.ZERO)) {
						direction = Vector3D.ofRandomUnitVector();
					}
					entity.mutableVelocity().add(direction.scaleToLength(strength));
				}
			});
		}));
		this.disposables.add(this.onUpdate().add(() -> {
			if (position().y() < -200f) {
				world().entities().remove(this);
			}
		}));
	}

	@Override
	public CollisionResponse getCollisionResponse() {
		return CollisionResponse.STOP;
	}

	@Override
	public void dispose() {
		disposables.dispose();
	}
}
