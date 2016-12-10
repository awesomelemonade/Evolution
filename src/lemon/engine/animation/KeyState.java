package lemon.engine.animation;

import lemon.engine.math.Vector;

public class KeyState {
	private Vector vector;
	private long time;
	public KeyState(Vector vector, long time){
		this.vector = vector;
		this.time = time;
	}
	public void setVector(Vector vector){
		this.vector = vector;
	}
	public Vector getVector(){
		return vector;
	}
	public void setTime(long time){
		this.time = time;
	}
	public long getTime(){
		return time;
	}
}
