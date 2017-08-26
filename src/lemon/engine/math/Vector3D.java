package lemon.engine.math;

import java.util.function.BinaryOperator;
import java.util.function.Function;

public class Vector3D extends Vector {
	public static final Function<float[], Vector3D> supplier = (data) -> new Vector3D(data);
	public static final Vector3D[] EMPTY_ARRAY = new Vector3D[]{};
	public static final Vector3D ZERO = Vector3D.unmodifiableVector(new Vector3D());
	public Vector3D(){
		this(0, 0, 0);
	}
	public Vector3D(float x, float y, float z){
		super(x, y, z);
	}
	public Vector3D(float[] data){
		if(data.length!=3){
			throw new IllegalArgumentException("Only 3 Dimensions Allowed");
		}
		this.setX(data[0]);
		this.setY(data[1]);
		this.setZ(data[2]);
	}
	public Vector3D(Vector vector){
		super(3);
		if(vector.getDimensions()!=3){
			throw new IllegalArgumentException("Only 3 Dimensions Allowed");
		}
		this.setX(vector.get(0));
		this.setY(vector.get(1));
		this.setZ(vector.get(2));
	}
	public Vector3D(Vector3D vector){
		super(vector);
	}
	public void setX(float x){
		this.set(0, x);
	}
	public float getX(){
		return this.get(0);
	}
	public void setY(float y){
		this.set(1, y);
	}
	public float getY(){
		return this.get(1);
	}
	public void setZ(float z){
		this.set(2, z);
	}
	public float getZ(){
		return this.get(2);
	}
	@Override
	public Vector3D normalize(){
		float magnitude = (float)Math.sqrt(Math.pow(this.getX(), 2)+Math.pow(this.getY(), 2)+Math.pow(this.getZ(), 2));
		return new Vector3D(this.getX()/magnitude, this.getY()/magnitude, this.getZ()/magnitude);
	}
	@Override
	public Vector3D getInvert(){
		return new Vector3D(-this.getX(), -this.getY(), -this.getZ());
	}
	public Vector3D add(Vector3D vector){
		return operate(vector, BasicFloatOperator.ADDITION);
	}
	public Vector3D subtract(Vector3D vector){
		return operate(vector, BasicFloatOperator.SUBTRACTION);
	}
	public Vector3D multiply(Vector3D vector){
		return operate(vector, BasicFloatOperator.MULTIPLICATION);
	}
	@Override
	public Vector3D multiply(float scale){
		return operate(scale, BasicFloatOperator.MULTIPLICATION);
	}
	public Vector3D divide(Vector3D vector){
		return operate(vector, BasicFloatOperator.DIVISION);
	}
	@Override
	public Vector3D divide(float scale){
		return operate(scale, BasicFloatOperator.DIVISION);
	}
	public Vector3D average(Vector3D vector){
		return operate(vector, BasicFloatOperator.AVERAGE);
	}
	public Vector3D operate(float scale, BinaryOperator<Float> operator){
		return new Vector3D(operator.apply(this.getX(), scale), operator.apply(this.getY(), scale), operator.apply(this.getZ(), scale));
	}
	public Vector3D operate(Vector3D vector, BinaryOperator<Float> operator){
		return new Vector3D(operator.apply(this.getX(), vector.getX()), operator.apply(this.getY(), vector.getY()), operator.apply(this.getZ(), vector.getZ()));
	}
	public Vector3D crossProduct(Vector3D vector){ //Implemented in only Vector3D
		return new Vector3D(this.getY()*vector.getZ()-vector.getY()*this.getZ(),
				this.getZ()*vector.getX()-vector.getZ()*this.getX(),
				this.getX()*vector.getY()-vector.getX()*this.getY());
	}
	public static Vector3D unmodifiableVector(Vector3D vector){
		return new Vector3D(vector){
			@Override
			public void set(Vector x){
				throw new IllegalStateException(unmodifiableMessage);
			}
			@Override
			public void set(int index, float data){
				throw new IllegalStateException(unmodifiableMessage);
			}
		};
	}
}
