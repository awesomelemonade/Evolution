package lemon.evolution.entity;

import lemon.engine.math.MathUtil;
import lemon.engine.math.Vector3D;
import lemon.engine.toolbox.Disposable;
import lemon.engine.toolbox.Disposables;
import lemon.evolution.world.AbstractEntity;
import lemon.evolution.world.Location;

import java.time.Duration;
import java.time.Instant;

public class RainmakerEntity extends AbstractEntity implements Disposable {
    private static final Duration DELAY = Duration.ofSeconds(1);
    private static final Duration ALIVE_DURATION = DELAY.plus(Duration.ofSeconds(5));
    private static final Duration RAIN_INTERVAL = Duration.ofSeconds(1);
    private final Disposables disposables = new Disposables();
    private final Instant creationTime;
    private boolean makeRaindroplets = false;
    private Instant lastRainTime = Instant.now();

    public RainmakerEntity(Location location, Vector3D velocity) {
        super(location, velocity);
        this.creationTime = Instant.now();
        disposables.add(this.onUpdate().add(() -> {
            // every time the world updates, this gets called
            // check if it's time to "explode" and start generating rain droplets
            if (Instant.now().isAfter(creationTime.plus(DELAY))) {
                makeRaindroplets = true;
                mutableVelocity().set(Vector3D.ZERO);
            }
            // We're only going to make rain for 5 seconds
            if (Instant.now().isAfter(creationTime.plus(ALIVE_DURATION))) {
                removeFromWorld();
            }
            if (makeRaindroplets) {
                // launch some "ExplodeOnHitProjectile"
                if (Duration.between(lastRainTime, Instant.now()).compareTo(RAIN_INTERVAL) > 0) {
                    var numRaindrops = 64;
                    var upwardVelocity = Vector3D.of(0f, 0.5f, 0f);
                    for (int i = 0; i < numRaindrops; i++) {
                        var angle = (float) (Math.random() * MathUtil.TAU);
                        var horizontalVelocity = Vector3D.of(MathUtil.cos(angle), 0, MathUtil.sin(angle)).multiply((float) (Math.random() * 1f));
                        world().entities().add(new ExplodeOnHitProjectile(location(), upwardVelocity.add(horizontalVelocity), ExplodeType.RAIN_DROPLET));
                    }
                    lastRainTime = Instant.now();
                }
            }
        }));
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

    @Override
    public void dispose() {
        disposables.dispose();
    }
}
