package lemon.evolution.entity;

import lemon.engine.math.Vector3D;
import lemon.engine.toolbox.Disposable;
import lemon.engine.toolbox.Disposables;
import lemon.evolution.world.AbstractEntity;
import lemon.evolution.world.ControllableEntity;
import lemon.evolution.world.Location;

import java.time.Duration;
import java.time.Instant;

public class TeleportBallEntity extends AbstractEntity implements Disposable {
    private static final Duration TELEPORT_DURATION = Duration.ofSeconds(2);
    private final Instant creationTime;
    private final Disposables disposables = new Disposables();

    public TeleportBallEntity(Location location, Vector3D velocity, ControllableEntity player) {
        super(location, velocity);
        this.creationTime = Instant.now();
        disposables.add(this.onUpdate().add(() -> {
            if (Instant.now().isAfter(creationTime.plus(TELEPORT_DURATION))) {
                removeFromWorld();
                player.mutablePosition().set(this.position());
            }
        }));
    }

    @Override
    public void dispose() {
        disposables.dispose();
    }


}
