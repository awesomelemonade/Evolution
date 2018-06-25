package lemon.engine.animation;

import java.util.function.BinaryOperator;

import lemon.engine.math.Vector;

public class FunctionInterpolator extends Interpolator {
	private final BinaryOperator<Float> function;

	public FunctionInterpolator(Vector vector, BinaryOperator<Float> function) {
		super(vector);
		this.function = function;
	}

	@Override
	public Vector interpolate(float value) {
		Vector result = new Vector(super.getVector().getDimensions());
		for (int i = 0; i < result.getDimensions(); ++i) {
			result.set(i, function.apply(super.getVector().get(i), value));
		}
		return result;
	}
}
