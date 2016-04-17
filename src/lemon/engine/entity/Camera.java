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
	public Camera(float fov, float aspectRatio, float nearPlane, float farPlane){
		this(new Vector(), new Vector(), fov, aspectRatio, nearPlane, farPlane);
	}
	public Camera(Vector position, Vector rotation, float fov, float aspectRatio, float nearPlane, float farPlane){
		this.position = position;
		this.rotation = rotation;
		this.fov = fov;
		this.aspectRatio = aspectRatio;
		this.nearPlane = nearPlane;
		this.farPlane = farPlane;
	}
	public Vector getPosition(){
		return position;
	}
	public Vector getRotation(){
		return rotation;
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
		return MathUtil.getPerspective(fov, aspectRatio, nearPlane, farPlane);
	}
}
