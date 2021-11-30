package lemon.engine.math;

public class OrbitCamera implements Camera {
    private final Vector3D target;
    public OrbitCamera(Vector3D target) {
        this.target = target;
    }

    @Override
    public Vector3D position() {
        return null;
    }

    @Override
    public Vector3D rotation() {
        return null;
    }

    @Override
    public Matrix projectionMatrix() {
        return null;
    }
}
