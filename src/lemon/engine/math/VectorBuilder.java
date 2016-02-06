package lemon.engine.math;

public class VectorBuilder {
	private float x;
	private float y;
	private float z;
	public VectorBuilder(){
		this.x = 0;
		this.y = 0;
		this.z = 0;
	}
	public VectorBuilder(Vector vector){
		this.x = vector.getX();
		this.y = vector.getY();
		this.z = vector.getZ();
	}
	public VectorBuilder add(float x, float y, float z){
		this.x+=x;
		this.y+=y;
		this.z+=z;
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
		this.x*=x;
		this.y*=y;
		this.z*=z;
		return this;
	}
	public VectorBuilder multiply(Vector vector){
		return multiply(vector.getX(), vector.getY(), vector.getZ());
	}
	public VectorBuilder divide(float x, float y, float z){
		this.x/=x;
		this.y/=y;
		this.z/=z;
		return this;
	}
	public VectorBuilder divide(Vector vector){
		return divide(vector.getX(), vector.getY(), vector.getZ());
	}
	public Vector toVector(){
		return new Vector(x, y, z);
	}
}
