package lemon.evolution.physics.beta;

import lemon.engine.math.Vector3D;

public class Collision {
	private float t; // time until collision (assuming constant velocity)
	private Vector3D intersection; // Where the object collides

	public Collision(float t, Vector3D intersection) {
		this.t = t;
		this.intersection = intersection;
	}

	public void test(float t, Vector3D intersection) {
		if (t < this.t) {
			this.t = t;
			this.intersection = intersection;
		}
	}

	public float getT() {
		return t;
	}

	public Vector3D getIntersection() {
		return intersection;
	}
}
