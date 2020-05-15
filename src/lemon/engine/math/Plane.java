package lemon.engine.math;

public class Plane {
	private float[] equation;
	private Vector3D origin;
	private Vector3D normal;
	public Plane(Triangle triangle) {
		this(triangle.getVertex1(), triangle.getVertex2(), triangle.getVertex3());
	}
	public Plane(Vector3D a, Vector3D b, Vector3D c) {
		this.origin = a;
		this.normal = b.copy().subtract(a).crossProduct(c.copy().subtract(a)).normalize();
		this.calculateEquation();
	}
	public Plane(Vector3D origin, Vector3D normal) {
		this.origin = origin;
		this.normal = normal;
		this.calculateEquation();
	}
	private void calculateEquation() {
		this.equation = new float[] {
				normal.getX(),
				normal.getY(),
				normal.getZ(),
				-normal.dotProduct(origin)
		};
	}
	public boolean isFrontFacingTo(Vector3D direction) {
		return normal.dotProduct(direction) <= 0;
	}
	public float getSignedDistanceTo(Vector3D point) {
		return point.dotProduct(normal) + this.equation[3];
	}
	public Vector3D getOrigin() {
		return origin;
	}
	public Vector3D getNormal() {
		return normal;
	}
}
