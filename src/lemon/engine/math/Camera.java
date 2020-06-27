package lemon.engine.math;

import java.util.function.Supplier;

public class Camera {
	private Vector3D position;
	private Vector3D rotation;
	private Projection projection;
	private Supplier<Matrix> transformationMatrixSupplier;
	private Supplier<Matrix> invertedRotationMatrixSupplier;

	public Camera(Projection projection) {
		this(new Vector3D(), new Vector3D(), projection);
	}
	public Camera(Vector3D position, Vector3D rotation, Projection projection) {
		this.position = position;
		this.rotation = rotation;
		this.projection = projection;
		Vector3D invertedPosition = new Vector3D();
		Vector3D invertedRotation = new Vector3D();
		Supplier<Matrix> rotationSupplier = MathUtil.getRotationSupplier(invertedRotation);
		Supplier<Matrix> transformationSupplier = MathUtil.getTransformationSupplier(invertedPosition, invertedRotation);
		this.invertedRotationMatrixSupplier = () -> {
			Vector.invert(invertedRotation, rotation);
			return rotationSupplier.get();
		};
		this.transformationMatrixSupplier = () -> {
			Vector.invert(invertedPosition, position);
			Vector.invert(invertedRotation, rotation);
			return transformationSupplier.get();
		};
	}
	public Vector3D getPosition() {
		return position;
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
