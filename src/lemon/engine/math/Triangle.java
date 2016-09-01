package lemon.engine.math;

public class Triangle extends VectorArray {
	public Triangle() {
		super(3);
	}
	public Triangle(Vector vector, Vector vector2, Vector vector3){
		this();
		this.set(0, vector);
		this.set(1, vector2);;
		this.set(2, vector3);
	}
}
