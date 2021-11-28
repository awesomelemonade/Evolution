package lemon.engine.math;

public interface CameraHolder extends Camera {
    @Override
    public default Vector3D position() {
        return camera().position();
    }

    @Override
    public default Vector3D rotation() {
        return camera().rotation();
    }

    @Override
    public default Matrix projectionMatrix() {
        return camera().projectionMatrix();
    }

    public Camera camera();
}
