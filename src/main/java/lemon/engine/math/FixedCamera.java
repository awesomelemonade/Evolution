package lemon.engine.math;

public record FixedCamera(Vector3D position, Quaternion rotation, Matrix projectionMatrix) implements Camera {
    public FixedCamera(Vector3D position, Quaternion rotation, Projection projection) {
        this(position, rotation, MathUtil.getPerspective(projection));
    }
}
