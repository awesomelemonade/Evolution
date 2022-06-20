package lemon.engine.math;

public class OrbitCamera implements Camera {
    private final Vector3D target;
    private final Matrix projectionMatrix;

    public OrbitCamera(Vector3D target, Projection projection) {
        this.target = target;
        this.projectionMatrix = MathUtil.getPerspective(projection);
    }

    @Override
    public Vector3D position() {
        return null;
    }

    @Override
    public Quaternion rotation() {
        return null;
    }

    @Override
    public Matrix projectionMatrix() {
        return projectionMatrix;
    }
}
