package lemon.engine.math;

import java.util.function.Supplier;

public class FirstPersonCamera implements Camera {
	private final MutableVector3D position;
	private final MutableVector3D rotation;
	private final Projection projection;
	private final Supplier<Matrix> transformationMatrixSupplier;
	private final Supplier<Matrix> invertedRotationMatrixSupplier;

	public FirstPersonCamera(Projection projection) {
		this(MutableVector3D.ofZero(), MutableVector3D.ofZero(), projection);
	}

	public FirstPersonCamera(Vector3D position, Vector3D rotation, Projection projection) {
		this(MutableVector3D.of(position), MutableVector3D.of(rotation), projection);
	}

	public FirstPersonCamera(MutableVector3D position, MutableVector3D rotation, Projection projection) {
		this.position = position;
		this.rotation = rotation;
		this.projection = projection;
		this.invertedRotationMatrixSupplier = MathUtil.getRotationSupplier(() -> this.rotation().invert());
		this.transformationMatrixSupplier = MathUtil.getTransformationSupplier(() -> this.position().invert(), () -> this.rotation().invert());
	}

	// Copy Constructor
	public FirstPersonCamera(FirstPersonCamera camera) {
		this(camera.position(), camera.rotation(), camera.projection());
	}

	public Vector3D position() {
		return position.asImmutable();
	}

	public MutableVector3D mutablePosition() {
		return position;
	}

	public Vector3D rotation() {
		return rotation.asImmutable();
	}

	public MutableVector3D mutableRotation() {
		return rotation;
	}

	public Projection projection() {
		return projection;
	}

	@Override
	public Matrix getTransformationMatrix() {
		return transformationMatrixSupplier.get();
	}

	@Override
	public Matrix getInvertedRotationMatrix() {
		return invertedRotationMatrixSupplier.get();
	}

	@Override
	public Matrix getProjectionMatrix() {
		return MathUtil.getPerspective(projection);
	}
}
