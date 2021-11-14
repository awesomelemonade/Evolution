package lemon.evolution.entity;

import lemon.engine.event.Observable;
import lemon.engine.game.Player;
import lemon.engine.math.MathUtil;
import lemon.engine.math.MutableVector3D;
import lemon.engine.math.Vector2D;
import lemon.engine.math.Vector3D;
import lemon.engine.toolbox.Disposable;
import lemon.engine.toolbox.Disposables;
import lemon.evolution.world.AbstractEntity;
import lemon.evolution.world.ControllableEntity;
import lemon.evolution.world.Location;

import java.time.Duration;
import java.time.Instant;

public class DrillEntity extends AbstractEntity implements Disposable {
    private final Disposables disposables = new Disposables();
    private final float SPEED = 1.0f;
    private final Duration FORWARD_TIME = Duration.ofSeconds(1);
    private Instant startTime;

    public DrillEntity(ControllableEntity player, Location location, Vector3D velocity) {
        super(location, velocity);
        startTime = Instant.now();
        disposables.add(this.onUpdate().add(() -> {
            // every time the world updates, this gets called
            if (Instant.now().isAfter(startTime)) {
                var mutableForce = player.mutableForce();
                var playerForwardVector = player.groundWatcher().groundParallel().orElse(player.vectorDirection()).multiply(SPEED);
                mutableForce.add(playerForwardVector);
            }
            if (Instant.now().isAfter(startTime.plus(FORWARD_TIME))) {
                removeFromWorld();
            }
        }));
    }

    @Override
    public void dispose() {
        disposables.dispose();
    }
}
