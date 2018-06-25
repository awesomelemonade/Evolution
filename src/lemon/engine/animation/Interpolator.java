package lemon.engine.animation;

import lemon.engine.math.Vector;

public abstract class Interpolator {
	private final Vector vector;

	public Interpolator(Vector vector) {
		this.vector = Vector.unmodifiableVector(vector);
	}

	public Vector getVector() {
		return vector;
	}

	public Vector interpolate(Vector vector) {
		return interpolate(vector.get(0));
	}

	public abstract Vector interpolate(float value);

	public static float clamp(float value) {
		if (value < 0f) {
			return 0f;
		}
		if (value > 1f) {
			return 1f;
		}
		return value;
	}
}
