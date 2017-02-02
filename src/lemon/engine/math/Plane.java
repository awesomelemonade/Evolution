package lemon.engine.math;

public class Plane {
	private Vector3D point;
	private Vector3D normal;
	public Plane(Vector3D point, Vector3D normal){
		this.point = point;
		this.normal = normal;
	}
	public Vector3D getPoint() {
		return point;
	}
	public void setPoint(Vector3D point) {
		this.point = point;
	}
	public Vector3D getNormal() {
		return normal;
	}
	public void setNormal(Vector3D normal) {
		this.normal = normal;
	}
}
