package lemon.engine.math;

public record MutableLine(MutableVector3D mutableOrigin, MutableVector3D mutableDirection) {
	public MutableLine() {
		this(MutableVector3D.of(0, 0, 0), MutableVector3D.of(0, 0, 0));
	}

	public MutableLine(Vector3D origin, Vector3D direction) {
		this(MutableVector3D.of(origin), MutableVector3D.of(direction));
	}

	public Vector3D origin() {
		return mutableOrigin.asImmutable();
	}

	public Vector3D direction() {
		return mutableDirection.asImmutable();
	}
}
