package lemon.engine.math;

public class Line extends VectorArray {
	public Line() {
		super(2);
	}
	public Line(Vector3D vector, Vector3D vector2){
		this();
		this.set(0, vector);
		this.set(1, vector2);
	}
	public Vector3D getOrigin(){
		return this.get(0);
	}
	public Vector3D getDirection(){
		return this.get(1);
	}
}
