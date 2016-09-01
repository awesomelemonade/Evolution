package lemon.engine.math;

public class Line extends VectorArray {
	public Line() {
		super(2);
	}
	public Line(Vector vector, Vector vector2){
		this();
		this.set(0, vector);
		this.set(1, vector2);
	}
}
