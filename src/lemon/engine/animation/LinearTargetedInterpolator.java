package lemon.engine.animation;

import lemon.engine.math.Vector;

public class LinearTargetedInterpolator implements TargetedInterpolator {
	private Vector vector;
	private KeyState target;
	public LinearTargetedInterpolator(Vector vector){
		this(vector, null);
	}
	public LinearTargetedInterpolator(Vector vector, KeyState target){
		this.vector = vector;
		this.target = target;
	}
	@Override
	public void update(long time) {
		if(target!=null){
			if(target.getTime()>0){
				float factor = ((float)time)/((float)this.getTarget().getTime());
				if(factor>1){
					factor = 1;
				}
				vector.set(vector.add(target.getVector().subtract(vector).multiply(factor)));
				target.setTime((long)(target.getTime()-(((float)this.getTarget().getTime())*factor)));
			}
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
	public void setTarget(KeyState target) {
		this.target = target;
	}
	@Override
	public KeyState getTarget() {
		return target;
	}
}
