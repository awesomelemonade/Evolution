package lemon.engine.math;

public class Vector {
	private float x;
	private float y;
	private float z;
	public Vector(){
		this(0f, 0f, 0f);
	}
	public Vector(float x, float y, float z){
		this.x = x;
		this.y = y;
		this.z = z;
	}
	public void setX(float x){
		this.x = x;
	}
	public void setY(float y){
		this.y = y;
	}
	public void setZ(float z){
		this.z = z;
	}
	public float getX(){
		return x;
	}
	public float getY(){
		return y;
	}
	public float getZ(){
		return z;
	}
	public Vector invert(){
		return new Vector(-x, -y, -z);
	}
	public float getDistance(Vector vector){
		return (float)Math.sqrt(Math.pow(vector.getX()-x, 2)+Math.pow(vector.getY()-y, 2)+Math.pow(vector.getZ()-z, 2));
	}
	@Override
	public String toString(){
		return "["+x+", "+y+", "+z+"]";
	}
	@Override
	public boolean equals(Object object){
		if(object==null){return false;}
		if(object instanceof Vector){
			Vector vector = (Vector)object;
			if(vector.getX()==x&&vector.getY()==y&&vector.getZ()==z){
				return true;
			}
		}
		return false;
	}
}