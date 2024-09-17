package lemon.evolution.physics.beta;

import lemon.engine.math.MutableVector3D;
import lemon.engine.math.Vector3D;

public class Collision {
	private float t = Float.MAX_VALUE; // time until collision (assuming constant velocity)
	private final MutableVector3D intersection = MutableVector3D.ofZero(); // Where the object collides
	private Vector3D negSlidePlaneNormal;

	public void setNegSlidePlaneNormal(Vector3D negSlidePlaneNormal) {
		this.negSlidePlaneNormal = negSlidePlaneNormal;
	}

	public void set(float t, Vector3D intersection) {
		this.t = t;
		this.intersection.set(intersection);
	}

	public void test(float t, Vector3D intersection) {
		if (t < this.t) {
			this.t = t;
			this.intersection.set(intersection);
		}
	}

	public float t() {
		return t;
	}

	public Vector3D intersection() {
		return intersection.asImmutable();
	}

	public Vector3D negSlidePlaneNormal() {
		return negSlidePlaneNormal;
	}

	@Override
	public String toString() {
		return String.format("Collision[t=%f, intersection=%s, negSlidePlaneNormal=%s]",
				t, intersection, negSlidePlaneNormal);
	}
}
