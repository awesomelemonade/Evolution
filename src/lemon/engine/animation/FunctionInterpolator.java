package lemon.engine.animation;

import java.util.function.Function;

import lemon.engine.math.Vector;

public class FunctionInterpolator implements Interpolator {
	private Function<Float, Float> function;
	private Vector vector;
	private Vector progress;
	private Vector change;
	private long startTime;
	private long endTime;
	public FunctionInterpolator(Vector vector, long startTime, long endTime, Vector change, Function<Float, Float> function){
		this.vector = vector;
		this.progress = new Vector(vector.getDimensions());
		this.startTime = startTime;
		this.change = change;
		this.endTime = endTime;
		this.function = function;
	}
	@Override
	public void update(long time) {
		if(time<startTime){
			time = startTime;
		}
		if(time>endTime){
			time = endTime;
		}
		float fraction = ((float)(time-startTime))/((float)(endTime-startTime));
		
		Vector x = change.multiply(function.apply(fraction));
		vector.set(vector.subtract(progress).add(x));
		progress = x;
	}
	@Override
	public void setVector(Vector vector) {
		this.vector = vector;
	}
	@Override
	public Vector getVector() {
		return vector;
	}
	public long getStartTime(){
		return startTime;
	}
	public long getEndTime(){
		return endTime;
	}
}
