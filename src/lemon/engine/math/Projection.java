package lemon.engine.math;

public class Projection {
	private float fov;
	private float aspectRatio;
	private float nearPlane;
	private float farPlane;
	public Projection(float fov, float aspectRatio, float nearPlane, float farPlane){
		this.fov = fov;
		this.aspectRatio = aspectRatio;
		this.nearPlane = nearPlane;
		this.farPlane = farPlane;
	}
	public float getFov() {
		return fov;
	}
	public void setFov(float fov) {
		this.fov = fov;
	}
	public float getAspectRatio() {
		return aspectRatio;
	}
	public void setAspectRatio(float aspectRatio) {
		this.aspectRatio = aspectRatio;
	}
	public float getNearPlane() {
		return nearPlane;
	}
	public void setNearPlane(float nearPlane) {
		this.nearPlane = nearPlane;
	}
	public float getFarPlane() {
		return farPlane;
	}
	public void setFarPlane(float farPlane) {
		this.farPlane = farPlane;
	}
}
