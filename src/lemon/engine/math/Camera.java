package lemon.engine.math;

import lemon.engine.math.MathUtil;
import lemon.engine.math.Matrix;
import lemon.engine.math.Vector;

public class Camera {
	private Vector position;
	private Vector rotation;
	private Projection projection;
	public Camera(Projection projection){
		this(new Vector(), new Vector(), projection);
	}
	public Camera(Vector position, Vector rotation, Projection projection){
		this.position = position;
		this.rotation = rotation;
		this.projection = projection;
	}
	public Vector getPosition(){
		return position;
	}
	public Vector getRotation(){
		return rotation;
	}
	public Projection getProjection(){
		return projection;
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
		return MathUtil.getPerspective(projection);
	}
}
