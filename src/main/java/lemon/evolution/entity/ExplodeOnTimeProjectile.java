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

	public ExplodeOnTimeProjectile(Location location, Vector3D velocity, ExplodeType type) {
		super(location, velocity, type.scalar());
		setType(type);
		this.creationTime = Instant.now();
		disposables.add(this.onUpdate().add(() -> {
			if (Instant.now().isAfter(creationTime.plus(2, ChronoUnit.SECONDS))) {
				removeFromWorld();
				world().generateExplosion(position(), type.explosionRadius());
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
}
