package lemon.engine.math;

public interface Camera {
    public Vector3D position();

    public Quaternion rotation();

    public Matrix projectionMatrix();
}
