package lemon.engine.animation;

import lemon.engine.math.Vector;

public class LinearInterpolator extends Interpolator {
	public LinearInterpolator(Vector vector) {
		super(vector);
	}
	@Override
	public Vector interpolate(float value) {
		Vector result = new Vector(super.getVector().getDimensions());
		for(int i=0;i<result.getDimensions();++i){
			result.set(i, super.getVector().get(i)*value);
		}
		return result;
	}
}
