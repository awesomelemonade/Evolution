package lemon.evolution.entity;
import lemon.engine.math.MathUtil;
import lemon.engine.math.MutableVector3D;
import lemon.engine.math.Vector3D;
import lemon.engine.toolbox.Disposable;
import lemon.engine.toolbox.Disposables;
import lemon.evolution.world.AbstractEntity;
import lemon.evolution.world.ControllableEntity;
import lemon.evolution.world.Location;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

public class TeleportBallEntity extends AbstractEntity implements Disposable {
    private final Instant creationTime;
    private final Disposables disposables = new Disposables();

    public TeleportBallEntity(Location location, Vector3D velocity, ControllableEntity player) {
        super(location, velocity);
        this.creationTime = Instant.now();
        disposables.add(this.onUpdate().add(() -> {
            if (Instant.now().isAfter(creationTime.plus(2, ChronoUnit.SECONDS))) {
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
