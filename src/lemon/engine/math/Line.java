package lemon.engine.math;

public class Line extends VectorArray {
	public Line() {
		super(2);
	}
	public Line(Vector3D vector, Vector3D vector2) {
		super(new Vector3D[]{vector, vector2});
	}
	public Vector3D getOrigin() {
		return this.get(0);
	}
	public Vector3D getDirection() {
		return this.get(1);
	}
	@Override
	public String toString(){
		return String.format("Line%s", super.toString());
	}
}
