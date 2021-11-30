package lemon.engine.math;

public interface Camera {
    public Vector3D position();

    public Vector3D rotation();

    public Matrix projectionMatrix();
}
