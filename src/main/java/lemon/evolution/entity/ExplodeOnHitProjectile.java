package lemon.evolution.entity;

import lemon.engine.math.Vector3D;
import lemon.engine.toolbox.Disposable;
import lemon.engine.toolbox.Disposables;
import lemon.evolution.physics.beta.CollisionResponse;
import lemon.evolution.world.AbstractEntity;
import lemon.evolution.world.Location;

public class ExplodeOnHitProjectile extends AbstractEntity implements Disposable {
	private final Disposables disposables = new Disposables();

	public ExplodeOnHitProjectile(Location location, Vector3D velocity, ExplodeType type) {
		super(location, velocity, type.scalar());
		setType(type);
		this.disposables.add(this.onCollide().add(explosionPosition -> {
			removeFromWorld();
			world().generateExplosion(explosionPosition, type.explosionRadius());
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
