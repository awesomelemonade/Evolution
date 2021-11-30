package lemon.engine.math;

public class MutableCamera implements Camera {
	private final MutableVector3D position;
	private final MutableVector3D rotation;
	private final Matrix projectionMatrix;

	public MutableCamera(Camera camera) {
		this(camera.position(), camera.rotation(), camera.projectionMatrix());
	}

	public MutableCamera(Projection projection) {
		this(MutableVector3D.ofZero(), MutableVector3D.ofZero(), projection);
	}

	public MutableCamera(Vector3D position, Vector3D rotation, Projection projection) {
		this(MutableVector3D.of(position), MutableVector3D.of(rotation), projection);
	}

	public MutableCamera(Vector3D position, Vector3D rotation, Matrix projectionMatrix) {
		this(MutableVector3D.of(position), MutableVector3D.of(rotation), projectionMatrix);
	}

	public MutableCamera(MutableVector3D position, MutableVector3D rotation, Projection projection) {
		this(position, rotation, MathUtil.getPerspective(projection));
	}

	public MutableCamera(MutableVector3D position, MutableVector3D rotation, Matrix projectionMatrix) {
		this.position = position;
		this.rotation = rotation;
		this.projectionMatrix = projectionMatrix;
	}

	@Override
	public Vector3D position() {
		return position.asImmutable();
	}

	public MutableVector3D mutablePosition() {
		return position;
	}

	@Override
	public Vector3D rotation() {
		return rotation.asImmutable();
	}

	public MutableVector3D mutableRotation() {
		return rotation;
	}

	public void setPositionAndRotation(Vector3D position, Vector3D rotation) {
		this.position.set(position);
		this.rotation.set(rotation);
	}

	public void setPositionAndRotation(Camera camera) {
		this.setPositionAndRotation(camera.position(), camera.rotation());
	}

	@Override
	public Matrix projectionMatrix() {
		return projectionMatrix;
	}
}
