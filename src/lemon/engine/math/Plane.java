package lemon.engine.math;

public record Plane(Vector3D origin, Vector3D normal, float[] equation) {
	public Plane(Triangle triangle) {
		this(triangle.a(), triangle.b(), triangle.c());
	}
	public Plane(Vector3D a, Vector3D b, Vector3D c) {
		this(a, b.subtract(a).crossProduct(c.subtract(a)).normalize());
	}
	public Plane(Vector3D origin, Vector3D normal) {
		this(origin, normal, new float[] {normal.x(), normal.y(), normal.z(), -normal.dotProduct(origin)});
	}
	public boolean isFrontFacingTo(Vector3D direction) {
		return normal.dotProduct(direction) <= 0;
	}
	public float getSignedDistanceTo(Vector3D point) {
		return point.dotProduct(normal) + this.equation[3];
	}
}
