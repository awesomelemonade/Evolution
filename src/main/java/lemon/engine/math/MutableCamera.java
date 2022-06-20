package lemon.engine.math;

public class MutableCamera implements Camera {
	private final MutableVector3D position;
	private final MutableQuaternion rotation;
	private final Matrix projectionMatrix;

	public MutableCamera(Camera camera) {
		this(camera.position(), camera.rotation(), camera.projectionMatrix());
	}

	public MutableCamera(Projection projection) {
		this(MutableVector3D.ofZero(), MutableQuaternion.ofZero(), projection);
	}

	public MutableCamera(Vector3D position, Quaternion rotation, Projection projection) {
		this(MutableVector3D.of(position), MutableQuaternion.of(rotation), projection);
	}

	public MutableCamera(Vector3D position, Quaternion rotation, Matrix projectionMatrix) {
		this(MutableVector3D.of(position), MutableQuaternion.of(rotation), projectionMatrix);
	}

	public MutableCamera(MutableVector3D position, MutableQuaternion rotation, Projection projection) {
		this(position, rotation, MathUtil.getPerspective(projection));
	}

	public MutableCamera(MutableVector3D position, MutableQuaternion rotation, Matrix projectionMatrix) {
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
	public Quaternion rotation() {
		return rotation.asImmutable();
	}

	public MutableQuaternion mutableRotation() {
		return rotation;
	}

	public void setPositionAndRotation(Vector3D position, Quaternion rotation) {
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
