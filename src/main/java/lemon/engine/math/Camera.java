package lemon.engine.math;

public interface Camera {
    public Matrix getTransformationMatrix();

    public Matrix getInvertedRotationMatrix();

    public Matrix getProjectionMatrix();
}
