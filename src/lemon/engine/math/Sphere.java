package lemon.engine.math;

public class Sphere {
	private Vector center;
	private float radius;
	public Sphere(Vector center, float radius){
		this.center = center;
		this.radius = radius;
	}
	public Vector getCenter(){
		return center;
	}
	public float getRadius(){
		return radius;
	}
}
