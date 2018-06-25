package lemon.engine.animation;

import java.util.function.UnaryOperator;

import lemon.engine.math.Vector;

public class AutomaticInterpolator {
	private Vector vector;
	private Interpolator interpolator;
	private UnaryOperator<Float> operator;
	private Vector prevUpdate;

	public AutomaticInterpolator(Vector vector, Interpolator interpolator, UnaryOperator<Float> operator) {
		this.vector = vector;
		this.interpolator = interpolator;
		this.operator = operator;
		this.prevUpdate = new Vector(vector.getDimensions());
	}

	public void update(float value) {
		Vector interpolated = interpolator.interpolate(operator.apply(value));
		vector.set(vector.subtract(prevUpdate).add(interpolated));
		prevUpdate = interpolated;
	}
}
