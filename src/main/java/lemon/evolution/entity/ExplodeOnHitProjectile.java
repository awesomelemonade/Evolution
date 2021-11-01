package lemon.evolution.entity;

import lemon.engine.math.Vector3D;
import lemon.engine.toolbox.Disposable;
import lemon.engine.toolbox.Disposables;
import lemon.evolution.physics.beta.CollisionResponse;
import lemon.evolution.world.AbstractEntity;
import lemon.evolution.world.Location;

public class ExplodeOnHitProjectile extends AbstractEntity implements Disposable {
	private final Disposables disposables = new Disposables();

	public ExplodeOnHitProjectile(Location location, Vector3D velocity, Type type) {
		super(location, velocity, type.scalar());
		this.meta().set("type", type);
		this.disposables.add(this.onCollide().add(explosionPosition -> {
			removeFromWorld();
			EntityUtil.generateExplosion(world(), explosionPosition);
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

	public enum Type {
		MISSILE(0.2f), RAIN_DROPLET(0.15f);

		private final Vector3D scalar;

		private Type(float size) {
			this(Vector3D.of(size, size, size));
		}

		private Type(Vector3D scalar) {
			this.scalar = scalar;
		}

		public Vector3D scalar() {
			return scalar;
		}
	}
}
