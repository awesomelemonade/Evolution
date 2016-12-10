package lemon.engine.animation;

import lemon.engine.math.Vector;

public class ExponentialTargetedInterpolator implements TargetedInterpolator {
	private Vector vector;
	private Vector target;
	private float factor; //per second (continuous?)
	
	public ExponentialTargetedInterpolator(Vector vector){
		this(vector, null, 1);
	}
	public ExponentialTargetedInterpolator(Vector vector, Vector target, float factor){
		this.vector = vector;
		this.target = target;
		this.factor = factor;
	}
	@Override
	public void update(long time) {
		if(target!=null){
			float changeFactor = (((float)time)/((float)1000000000))*factor;
			vector.set(vector.add(target.subtract(vector).multiply(changeFactor)));
		}
	}
	@Override
	public void setVector(Vector vector) {
		this.vector = vector;
	}
	@Override
	public Vector getVector() {
		return vector;
	}
	@Override
	public void setTarget(Vector target) {
		this.target = target;
	}
	@Override
	public Vector getTarget() {
		return target;
	}
}
