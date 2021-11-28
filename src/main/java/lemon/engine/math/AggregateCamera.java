package lemon.engine.math;

import lemon.engine.event.Observable;
import lemon.engine.toolbox.Disposables;

import java.time.Duration;
import java.time.Instant;

public class AggregateCamera implements CameraHolder {
    private static final Duration TRANSITION_TIME = Duration.ofSeconds(1);
    private final Observable<Camera> camera;

    public AggregateCamera(Camera camera) {
        this.camera = new Observable<>(camera);
    }

    public void interpolateTo(Camera camera) {
        var newCamera = new InterpolatedCameraByTime(camera(), camera, Instant.now(), TRANSITION_TIME);
        var disposeOnDone = new Disposables();
        disposeOnDone.add(newCamera.observableDone().onChangeTo(true, () -> {
            if (camera() == newCamera) {
                set(newCamera.to());
            }
            disposeOnDone.dispose();
        }));
        set(newCamera);
    }

    public void set(Camera camera) {
        this.camera.setValue(camera);
    }

    @Override
    public Camera camera() {
        return camera.getValue();
    }

    public Observable<Camera> observableCamera() {
        return camera;
    }
}
