package lemon.engine.animation;

import java.util.function.Function;

import lemon.engine.math.Vector;

public class FunctionInterpolator implements Interpolator {
	private Function<Float, Vector> function;
	private Vector vector;
	private Vector progress;
	private long time;
	private long totalTime;
	public FunctionInterpolator(Vector vector, long totalTime, Function<Float, Vector> function){
		this.vector = vector;
		this.progress = new Vector(vector.getDimensions());
		this.time = 0;
		this.totalTime = totalTime;
		this.function = function;
	}
	@Override
	public void update(long time) {
		if(time<totalTime){
			this.time+=time;
			if(this.time>totalTime){
				this.time = totalTime;
			}
			
			float fraction = ((float)this.time)/((float)totalTime);
			Vector x = function.apply(fraction);
			vector.set(vector.subtract(progress).add(x));
			progress = x;
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
	public void setTime(long time){
		this.time = time;
	}
	public long getTime(){
		return time;
	}
	public void setTotalTime(long time){
		this.totalTime = time;
	}
	public long getTotalTime(){
		return totalTime;
	}
}
