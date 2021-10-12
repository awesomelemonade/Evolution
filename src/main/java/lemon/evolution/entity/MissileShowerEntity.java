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
		super(location, velocity);
		this.creationTime = Instant.now();
		disposables.add(this.onUpdate().add(() -> {
			if (Instant.now().isAfter(creationTime.plus(1, ChronoUnit.SECONDS))) {
				removeFromWorld();
				var numRockets = 256;
				var upwardVelocity = Vector3D.of(0f, 1f, 0f);
				for (int i = 0; i < numRockets; i++) {
					var angle = (float) (Math.random() * MathUtil.TAU);
					var horizontalVelocity = Vector3D.of(MathUtil.cos(angle), 0, MathUtil.sin(angle)).multiply((float) (Math.random() * 1f));
					world().entities().add(new RocketLauncherProjectile(location(), upwardVelocity.add(horizontalVelocity)));
				}
			}
		}));
	}

	@Override
	public void dispose() {
		disposables.dispose();
	}
}
