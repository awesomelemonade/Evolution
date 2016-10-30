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
	public Vector getVertex1(){
		return this.get(0);
	}
	public Vector getVertex2(){
		return this.get(1);
	}
	public Vector getVertex3(){
		return this.get(2);
	}
}
