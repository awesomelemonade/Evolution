package lemon.engine.animation;

import lemon.engine.math.Vector;

public class LinearTargetedInterpolator implements TargetedInterpolator {
	private Vector vector;
	private Vector target;
	private long time;
	public LinearTargetedInterpolator(Vector vector){
		this(vector, null, 0);
	}
	public LinearTargetedInterpolator(Vector vector, Vector target, long time){
		this.vector = vector;
		this.target = target;
		this.time = time;
	}
	@Override
	public void update(long time) {
		if(this.time>0){
			float factor = ((float)time)/((float)this.time);
			if(factor>1){
				factor = 1;
			}
			vector.set(vector.add(target.subtract(vector).multiply(factor)));
			this.time-=((long)((((float)this.time)*factor)));
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
	public void setTime(long time){
		this.time = time;
	}
	public long getTime(){
		return time;
	}
}
