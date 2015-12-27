package lemon.engine.math;

public class VectorBuilder {
	private Vector vector;
	private VectorBuilder(Vector vector){
		this.vector = vector;
	}
	public static VectorBuilder get(Vector vector){
		return new VectorBuilder(new Vector(vector));
	}
	public VectorBuilder add(float x, float y, float z){
		vector.setX(vector.getX()+x);
		vector.setY(vector.getY()+y);
		vector.setZ(vector.getZ()+z);
		return this;
	}
	public VectorBuilder add(Vector vector){
		return add(vector.getX(), vector.getY(), vector.getZ());
	}
	public VectorBuilder subtract(float x, float y, float z){
		return add(-x, -y, -z);
	}
	public VectorBuilder subtract(Vector vector){
		return add(-vector.getX(), -vector.getY(), -vector.getZ());
	}
	public VectorBuilder multiply(float x, float y, float z){
		vector.setX(vector.getX()*x);
		vector.setY(vector.getY()*y);
		vector.setZ(vector.getZ()*z);
		return this;
	}
	public VectorBuilder multiply(Vector vector){
		return multiply(vector.getX(), vector.getY(), vector.getZ());
	}
	public VectorBuilder divide(float x, float y, float z){
		vector.setX(vector.getX()/x);
		vector.setY(vector.getY()/y);
		vector.setZ(vector.getZ()/z);
		return this;
	}
	public VectorBuilder divide(Vector vector){
		return divide(vector.getX(), vector.getY(), vector.getZ());
	}
	public Vector toVector(){
		return vector;
	}
}
