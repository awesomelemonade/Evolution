package lemon.engine.math;

public class Sphere {
	private Vector3D center;
	private float radius;

	public Sphere(Vector3D center, float radius) {
		this.center = center;
		this.radius = radius;
	}
	public Vector3D getCenter() {
		return center;
	}
	public float getRadius() {
		return radius;
	}
}
