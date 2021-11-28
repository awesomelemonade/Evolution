package lemon.engine.math;

public class MutableCamera implements Camera {
	private final MutableVector3D position;
	private final MutableVector3D rotation;
	private final Projection projection;

	public MutableCamera(Projection projection) {
		this(MutableVector3D.ofZero(), MutableVector3D.ofZero(), projection);
	}

	public MutableCamera(Vector3D position, Vector3D rotation, Projection projection) {
		this(MutableVector3D.of(position), MutableVector3D.of(rotation), projection);
	}

	public MutableCamera(MutableVector3D position, MutableVector3D rotation, Projection projection) {
		this.position = position;
		this.rotation = rotation;
		this.projection = projection;
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

	public Projection projection() {
		return projection;
	}

	@Override
	public Matrix projectionMatrix() {
		return MathUtil.getPerspective(projection);
	}

	public static MutableCamera ofCopy(MutableCamera camera) {
		return new MutableCamera(camera.position(), camera.rotation(), camera.projection());
	}
}
