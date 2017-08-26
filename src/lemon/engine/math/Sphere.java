package lemon.engine.math;

public class Sphere {
	private Vector3D center;
	private Vector3D axes;
	public Sphere(Vector3D center, Vector3D axes){
		this.center = center;
		this.axes = axes;
	}
	public Vector3D getCenter(){
		return center;
	}
	public Vector3D getAxes(){
		return axes;
	}
}
