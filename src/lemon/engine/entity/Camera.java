package lemon.engine.entity;

import lemon.engine.math.MathUtil;
import lemon.engine.math.Matrix;
import lemon.engine.math.Vector;

public class Camera {
	private Vector position;
	private Vector rotation;
	private float fov;
	private float aspectRatio;
	private float nearPlane;
	private float farPlane;
	private float zoom;
	public Camera(float fov, float aspectRatio, float nearPlane, float farPlane){
		this(new Vector(), new Vector(), fov, aspectRatio, nearPlane, farPlane, 1f);
	}
	public Camera(Vector position, Vector rotation, float fov, float aspectRatio, float nearPlane, float farPlane, float zoom){
		this.position = position;
		this.rotation = rotation;
		this.fov = fov;
		this.aspectRatio = aspectRatio;
		this.nearPlane = nearPlane;
		this.farPlane = farPlane;
		this.zoom = zoom;
	}
	public Vector getPosition(){
		return position;
	}
	public Vector getRotation(){
		return rotation;
	}
	public void setZoom(float zoom){
		this.zoom = zoom;
	}
	public float getZoom(){
		return zoom;
	}
	public Matrix getInvertedTranslationMatrix(){
		Vector translation = this.position.getInvert();
		return MathUtil.getTranslation(translation);
	}
	public Matrix getInvertedRotationMatrix(){
		Vector rotation = this.rotation.getInvert();
		return MathUtil.getRotationX(rotation.getX()).multiply(MathUtil.getRotationY(rotation.getY()).multiply(MathUtil.getRotationZ(rotation.getZ())));
	}
	public Matrix getProjectionMatrix(){
		return MathUtil.getPerspective(fov*zoom, aspectRatio, nearPlane, farPlane);
	}
}
