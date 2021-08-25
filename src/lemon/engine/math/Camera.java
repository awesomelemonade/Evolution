package lemon.engine.math;

import java.util.function.Supplier;

public class Camera {
	private Vector3D position;
	private Vector3D rotation;
	private Projection projection;
	private Supplier<Matrix> transformationMatrixSupplier;
	private Supplier<Matrix> invertedRotationMatrixSupplier;

	public Camera(Projection projection) {
		this(Vector3D.ZERO, Vector3D.ZERO, projection);
	}
	public Camera(Vector3D position, Vector3D rotation, Projection projection) {
		this.position = position;
		this.rotation = rotation;
		this.projection = projection;
		this.invertedRotationMatrixSupplier = MathUtil.getRotationSupplier(() -> this.getRotation().invert());
		this.transformationMatrixSupplier = MathUtil.getTransformationSupplier(() -> this.getPosition().invert(), () -> this.getRotation().invert());
	}
	public void setPosition(Vector3D position) {
		this.position = position;
	}
	public Vector3D getPosition() {
		return position;
	}
	public void setRotation(Vector3D rotation) {
		this.rotation = rotation;
	}
	public Vector3D getRotation() {
		return rotation;
	}
	public Projection getProjection() {
		return projection;
	}
	public Matrix getTransformationMatrix() {
		return transformationMatrixSupplier.get();
	}
	public Matrix getInvertedRotationMatrix() {
		return invertedRotationMatrixSupplier.get();
	}
	public Matrix getProjectionMatrix() {
		return MathUtil.getPerspective(projection);
	}
}
