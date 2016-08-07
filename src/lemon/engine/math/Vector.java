package lemon.engine.math;

public class Vector {
	public static final Vector[] EMPTY_ARRAY = new Vector[]{};
	public static final Vector ZERO = Vector.unmodifiableVector(new Vector());
	private static final String unmodifiableMessage = "Cannot Modify Vector";
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
	public Vector(Vector vector){
		this(vector.getX(), vector.getY(), vector.getZ());
	}
	public void set(Vector vector){
		this.x = vector.getX();
		this.y = vector.getY();
		this.z = vector.getZ();
	}
	public void setX(float x){
		this.x = x;
	}
	public float getX(){
		return x;
	}
	public void setY(float y){
		this.y = y;
	}
	public float getY(){
		return y;
	}
	public void setZ(float z){
		this.z = z;
	}
	public float getZ(){
		return z;
	}
	public Vector getInvert(){
		return new Vector(-x, -y, -z);
	}
	public float getDistance(Vector vector){
		return (float)Math.sqrt(Math.pow(vector.getX()-x, 2)+Math.pow(vector.getY()-y, 2)+Math.pow(vector.getZ()-z, 2));
	}
	public Vector add(Vector vector){
		return new Vector(x+vector.getX(), y+vector.getY(), z+vector.getZ());
	}
	public Vector subtract(Vector vector){
		return new Vector(x-vector.getX(), y-vector.getY(), z-vector.getZ());
	}
	public Vector multiply(Vector vector){
		return new Vector(x*vector.getX(), y*vector.getY(), z*vector.getZ());
	}
	public Vector divide(Vector vector){
		return new Vector(x/vector.getX(), y/vector.getY(), z/vector.getZ());
	}
	@Override
	public String toString(){
		return "["+x+", "+y+", "+z+"]";
	}
	@Override
	public boolean equals(Object object){
		if(object==null){
			return false;
		}
		if(object instanceof Vector){
			Vector vector = (Vector)object;
			if(vector.getX()==x&&vector.getY()==y&&vector.getZ()==z){
				return true;
			}
		}
		return false;
	}
	public static Vector unmodifiableVector(Vector vector){
		return new Vector(vector){
			@Override
			public void setX(float x){
				throw new IllegalStateException(unmodifiableMessage);
			}
			@Override
			public void setY(float y){
				throw new IllegalStateException(unmodifiableMessage);
			}
			@Override
			public void setZ(float z){
				throw new IllegalStateException(unmodifiableMessage);
			}
		};
	}
}