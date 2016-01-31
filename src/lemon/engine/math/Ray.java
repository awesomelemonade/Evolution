package lemon.engine.math;

public class Ray {
	private Vector origin;
	private Vector direction;
	public Ray(Vector origin, Vector direction){
		this.origin = origin;
		this.direction = direction;
	}
	public Vector getOrigin(){
		return origin;
	}
	public Vector getDirection(){
		return direction;
	}
}
