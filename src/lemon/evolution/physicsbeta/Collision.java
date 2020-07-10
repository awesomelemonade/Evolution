package lemon.evolution.physicsbeta;

import lemon.engine.math.Triangle;
import lemon.engine.math.Vector3D;

public class Collision {
	private float t; // time until collision (assuming constant velocity)
	private Vector3D intersection; // Where the object collides
	private Triangle triangle;
	public String reason;
	public Collision(float t, Vector3D intersection) {
		this.t = t;
		this.intersection = intersection;
	}
	public void set(float t, Vector3D intersection, Triangle triangle, String reason) {
		this.t = t;
		this.intersection.set(intersection);
		this.triangle = triangle;
		this.reason = reason;
	}
	public void test(float t, Vector3D intersection, Triangle triangle, String reason) {
		if (t < this.t) {
			this.t = t;
			this.intersection.set(intersection);
			this.triangle = triangle;
			this.reason = reason;
		}
	}
	public float getT() {
		return t;
	}
	public Vector3D getIntersection() {
		return intersection;
	}
	public Triangle getTriangle() {
		return triangle;
	}
}
