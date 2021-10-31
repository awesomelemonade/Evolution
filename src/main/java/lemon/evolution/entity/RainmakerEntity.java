package lemon.evolution.entity;

import lemon.engine.math.MathUtil;
import lemon.engine.math.Vector3D;
import lemon.evolution.world.AbstractEntity;
import lemon.evolution.world.Location;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

public class RainmakerEntity extends AbstractEntity {
    private final Instant creationTime;
    private boolean makeRaindroplets = false;
    private Instant lastRainTime = Instant.now();

    public RainmakerEntity(Location location, Vector3D velocity) {
        super(location, velocity);
        this.creationTime = Instant.now();
        this.onUpdate().add(() -> {
            // every time the world updates, this gets called
            // check if it's time to "explode" and start generating rain droplets
            if (Instant.now().isAfter(creationTime.plus(3, ChronoUnit.SECONDS))) {
                makeRaindroplets = true;
            }
            // We're only going to make rain for 5 seconds
            if (Instant.now().isAfter(creationTime.plus(8, ChronoUnit.SECONDS))) {
                removeFromWorld();
            }
            if (makeRaindroplets) {
                // launch some "ExplodeOnHitProjectile"
                if (Duration.between(lastRainTime, Instant.now()).compareTo(Duration.of(1, ChronoUnit.SECONDS)) > 0) {
                    var numRockets = 256;
                    var upwardVelocity = Vector3D.of(0f, 1f, 0f);
                    for (int i = 0; i < numRockets; i++) {
                        var angle = (float) (Math.random() * MathUtil.TAU);
                        var horizontalVelocity = Vector3D.of(MathUtil.cos(angle), 0, MathUtil.sin(angle)).multiply((float) (Math.random() * 1f));
                        world().entities().add(new ExplodeOnHitProjectile(location, upwardVelocity.add(horizontalVelocity), ExplodeOnHitProjectile.Type.RAIN_DROPLET));
                    }
                    lastRainTime = Instant.now();
                }
            }
        });
    }

    @Override
    public Vector3D getEnvironmentalForce() {
        if (makeRaindroplets) {
            return Vector3D.ZERO;
        } else {
            // Default behavior
            return super.getEnvironmentalForce();
        }
    }
}
