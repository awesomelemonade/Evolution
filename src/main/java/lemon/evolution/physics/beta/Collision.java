package lemon.evolution.physics.beta;

import lemon.engine.math.Vector3D;

public class Collision {
	private float t = Float.MAX_VALUE; // time until collision (assuming constant velocity)
	private Vector3D intersection; // Where the object collides
	private Vector3D usedVelocity;
	private Vector3D negSlidePlaneNormal;
	private CollisionResponse response = CollisionResponse.SLIDE;

	public void calc(Vector3D usedVelocity, Vector3D negSlidePlaneNormal) {
		this.usedVelocity = usedVelocity;
		this.negSlidePlaneNormal = negSlidePlaneNormal;
	}

	public void set(float t, Vector3D intersection) {
		this.t = t;
		this.intersection = intersection;
	}

	public void test(float t, Vector3D intersection) {
		if (t < this.t) {
			this.t = t;
			this.intersection = intersection;
		}
	}

	public float t() {
		return t;
	}

	public Vector3D intersection() {
		return intersection;
	}

	public Vector3D usedVelocity() {
		return usedVelocity;
	}

	public Vector3D negSlidePlaneNormal() {
		return negSlidePlaneNormal;
	}

	public void setResponse(CollisionResponse response) {
		this.response = response;
	}

	public CollisionResponse response() {
		return response;
	}

	@Override
	public String toString() {
		return String.format("Collision[t=%f, intersection=%s, usedVelocity=%s, negSlidePlaneNormal=%s]",
				t, intersection, usedVelocity, negSlidePlaneNormal);
	}
}
