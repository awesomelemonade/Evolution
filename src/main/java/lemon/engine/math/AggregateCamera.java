package lemon.engine.math;

import java.time.Duration;
import java.time.Instant;

public class AggregateCamera implements CameraHolder {
    private static final Duration TRANSITION_TIME = Duration.ofSeconds(1);
    private Camera camera;

    public AggregateCamera(Camera camera) {
        this.camera = camera;
    }

    public void interpolateTo(Camera camera) {
        this.camera = new InterpolatedCameraByTime(this.camera, camera, Instant.now(), TRANSITION_TIME);
    }

    public void set(Camera camera) {
        this.camera = camera;
    }

    @Override
    public Camera camera() {
        return camera;
    }
}
