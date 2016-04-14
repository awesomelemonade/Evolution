package lemon.engine.entity;

import lemon.engine.math.MathUtil;
import lemon.engine.math.Matrix;
import lemon.engine.math.Vector;

public class Camera {
	private Vector position;
	private Vector rotation;
	public Camera(){
		this(new Vector(), new Vector());
	}
	public Camera(Vector position, Vector rotation){
		this.position = position;
		this.rotation = rotation;
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
}
