package lemon.evolution.entity;

import lemon.engine.math.Vector3D;
import lemon.engine.toolbox.Disposable;
import lemon.engine.toolbox.Disposables;
import lemon.evolution.physics.beta.CollisionResponse;
import lemon.evolution.world.AbstractEntity;
import lemon.evolution.world.Location;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

public class ExplodeOnTimeProjectile extends AbstractEntity implements Disposable {
	private final Instant creationTime;
	private final Disposables disposables = new Disposables();

	public ExplodeOnTimeProjectile(Location location, Vector3D velocity, Type type) {
		super(location, velocity, type.scalar());
		this.meta().set("type", type);
		this.creationTime = Instant.now();
		disposables.add(this.onUpdate().add(() -> {
			if (Instant.now().isAfter(creationTime.plus(2, ChronoUnit.SECONDS))) {
				removeFromWorld();
				EntityUtil.generateExplosion(world(), position());
			}
		}));
	}

	@Override
	public CollisionResponse getCollisionResponse() {
		return CollisionResponse.SLIDE;
	}

	@Override
	public void dispose() {
		disposables.dispose();
	}

	public enum Type {
		GRENADE(0.2f);

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
