package lemon.engine.math;

public class MutableTriangle implements Triangle {
	private final MutableVector3D a = MutableVector3D.ofZero();
	private final MutableVector3D b = MutableVector3D.ofZero();
	private final MutableVector3D c = MutableVector3D.ofZero();
	private final MutableVector3D normal = MutableVector3D.ofZero();
	private float area = 0f;

	public void setAndDivideByScalar(Triangle triangle, Vector3D scalar, Vector3D scalarSquared) {
		a.set(triangle.a()).divide(scalar);
		b.set(triangle.b()).divide(scalar);
		c.set(triangle.c()).divide(scalar);
		normal.set(triangle.normal()).divide(scalarSquared);
		float length = normal.asImmutable().length();
		area = length * triangle.area();
		if (length > 0f) {
			normal.divide(length);
		}
	}

	@Override
	public Vector3D a() {
		return a.asImmutable();
	}

	@Override
	public Vector3D b() {
		return b.asImmutable();
	}

	@Override
	public Vector3D c() {
		return c.asImmutable();
	}

	@Override
	public Vector3D normal() {
		return normal.asImmutable();
	}

	@Override
	public float area() {
		return area;
	}
}
