package lemon.engine.math;

import lemon.engine.math.MathUtil;
import lemon.engine.math.Matrix;

public class Camera {
	private Vector3D position;
	private Vector3D rotation;
	private Projection projection;
	public Camera(Projection projection){
		this(new Vector3D(), new Vector3D(), projection);
	}
	public Camera(Vector3D position, Vector3D rotation, Projection projection){
		this.position = position;
		this.rotation = rotation;
		this.projection = projection;
	}
	public Vector3D getPosition(){
		return position;
	}
	public Vector3D getRotation(){
		return rotation;
	}
	public Projection getProjection(){
		return projection;
	}
	public Matrix getInvertedTranslationMatrix(){
		return MathUtil.getTranslation(this.position.getInvert());
	}
	public Matrix getInvertedRotationMatrix(){
		Vector3D rotation = this.rotation.getInvert();
		return MathUtil.getRotationX(rotation.getX()).multiply(MathUtil.getRotationY(rotation.getY()).multiply(MathUtil.getRotationZ(rotation.getZ())));
	}
	public Matrix getProjectionMatrix(){
		return MathUtil.getPerspective(projection);
	}
}
