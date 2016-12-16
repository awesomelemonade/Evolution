package lemon.engine.math;

public class Triangle extends VectorArray {
	public Triangle() {
		super(3);
	}
	public Triangle(Vector3D vector, Vector3D vector2, Vector3D vector3){
		this();
		this.set(0, vector);
		this.set(1, vector2);
		this.set(2, vector3);
	}
	public Vector3D getVertex1(){
		return this.get(0);
	}
	public Vector3D getVertex2(){
		return this.get(1);
	}
	public Vector3D getVertex3(){
		return this.get(2);
	}
}
