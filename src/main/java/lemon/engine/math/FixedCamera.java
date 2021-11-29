package lemon.engine.math;

public record FixedCamera(Vector3D position, Vector3D rotation, Matrix projectionMatrix) implements Camera {
    public FixedCamera(Vector3D position, Vector3D rotation, Projection projection) {
        this(position, rotation, MathUtil.getPerspective(projection));
    }
}
