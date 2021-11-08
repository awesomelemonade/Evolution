package lemon.evolution.entity;

import lemon.engine.math.MathUtil;
import lemon.engine.math.Vector3D;
import lemon.engine.toolbox.Disposable;
import lemon.engine.toolbox.Disposables;
import lemon.evolution.world.AbstractEntity;
import lemon.evolution.world.Location;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

public class MissileShowerEntity extends AbstractEntity implements Disposable {
	private final Instant creationTime;
	private final Disposables disposables = new Disposables();

	public MissileShowerEntity(Location location, Vector3D velocity) {
		super(location, velocity, Vector3D.of(0.2f, 0.2f, 0.2f));
		this.creationTime = Instant.now();
		disposables.add(this.onUpdate().add(() -> {
			if (Instant.now().isAfter(creationTime.plus(1, ChronoUnit.SECONDS))) {
				explode(location());
			}
		}));
		disposables.add(this.onCollide().add(intersection -> {
			explode(new Location(world(), intersection));
		}));
	}

	public void explode(Location location) {
		removeFromWorld();
		var numRockets = 32;
		var upwardVelocity = Vector3D.of(0f, 0.5f, 0f);
		for (int i = 0; i < numRockets; i++) {
			var angle = (float) (Math.random() * MathUtil.TAU);
			var horizontalVelocity = Vector3D.of(MathUtil.cos(angle), 0, MathUtil.sin(angle)).multiply((float) (Math.random() * 1f));
			world().entities().add(new ExplodeOnHitProjectile(location, upwardVelocity.add(horizontalVelocity), ExplodeType.MINI_MISSILE));
		}
	}

	@Override
	public void dispose() {
		disposables.dispose();
	}
}
