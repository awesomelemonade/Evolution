package lemon.engine.math;

import java.util.Arrays;
import java.util.function.BinaryOperator;

public class Vector {
	protected static final String unmodifiableMessage = "Cannot Modify Vector";
	private float[] data;
	
	public Vector(int length){
		this.data = new float[length];
	}
	public Vector(Vector vector){
		float[] data = new float[vector.getDimensions()];
		for(int i=0;i<data.length;++i){
			data[i] = vector.get(i);
		}
		this.data = data;
	}
	public Vector(float... data){
		this.data = data;
	}
	public int getDimensions(){
		return data.length;
	}
	private void checkDimensions(Vector vector){
		if(this.getDimensions()!=vector.getDimensions()){
			throw new IllegalArgumentException("Dimensions are not equal");
		}
	}
	public void set(Vector vector){
		checkDimensions(vector);
		for(int i=0;i<data.length;++i){
			data[i] = vector.get(i);
		}
	}
	public void set(int index, float data){
		this.data[index] = data;
	}
	public float get(int index){
		return data[index];
	}
	public Vector normalize(){
		float magnitude = 0;
		for(int i=0;i<data.length;++i){
			magnitude+=Math.pow(data[i], 2);
		}
		magnitude = (float)Math.sqrt(magnitude);
		float[] data = new float[this.data.length];
		for(int i=0;i<data.length;++i){
			data[i] = this.data[i]/magnitude;
		}
		return new Vector(data);
	}
	public Vector getInvert(){
		float[] data = new float[this.data.length];
		for(int i=0;i<data.length;++i){
			data[i] = -this.data[i];
		}
		return new Vector(data);
	}
	public float getAbsoluteValue(){
		return (float)Math.sqrt(getAbsoluteValueSquared());
	}
	public float getAbsoluteValueSquared(){
		float sum = 0;
		for(int i=0;i<data.length;++i){
			sum+=Math.pow(data[i], 2);
		}
		return sum;
	}
	public float getDistance(Vector vector){
		return (float)Math.sqrt(getDistanceSquared(vector));
	}
	public float getDistanceSquared(Vector vector){
		checkDimensions(vector);
		float sum = 0;
		for(int i=0;i<data.length;++i){
			sum+=Math.pow(vector.get(i)-data[i], 2);
		}
		return sum;
	}
	public Vector add(Vector vector){
		return operate(vector, BasicFloatOperator.ADDITION);
	}
	public Vector subtract(Vector vector){
		return operate(vector, BasicFloatOperator.SUBTRACTION);
	}
	public Vector multiply(Vector vector){
		return operate(vector, BasicFloatOperator.MULTIPLICATION);
	}
	public Vector multiply(float scale){
		return operate(scale, BasicFloatOperator.MULTIPLICATION);
	}
	public Vector divide(Vector vector){
		return operate(vector, BasicFloatOperator.DIVISION);
	}
	public Vector divide(float scale){
		return operate(scale, BasicFloatOperator.DIVISION);
	}
	public Vector average(Vector vector){
		return operate(vector, BasicFloatOperator.AVERAGE);
	}
	public Vector operate(float scale, BinaryOperator<Float> operator){
		float[] data = new float[this.data.length];
		for(int i=0;i<data.length;++i){
			data[i] = operator.apply(this.data[i], scale);
		}
		return new Vector(data);
	}
	public Vector operate(Vector vector, BinaryOperator<Float> operator){
		checkDimensions(vector);
		float[] data = new float[this.data.length];
		for(int i=0;i<data.length;++i){
			data[i] = operator.apply(this.data[i], vector.get(i));
		}
		return new Vector(data);
	}
	public float dotProduct(Vector vector){
		checkDimensions(vector);
		float sum = 0;
		for(int i=0;i<data.length;++i){
			sum+=data[i]*vector.get(i);
		}
		return sum;
	}
	public static final Vector2D TOP_LEFT = Vector2D.unmodifiableVector(new Vector2D(-1f, 1f).normalize());
	public static final Vector2D TOP = Vector2D.unmodifiableVector(new Vector2D(0f, 1f).normalize());
	public static final Vector2D TOP_RIGHT = Vector2D.unmodifiableVector(new Vector2D(1f, 1f).normalize());
	public static final Vector2D LEFT = Vector2D.unmodifiableVector(new Vector2D(-1f, 0f).normalize());
	public static final Vector2D RIGHT = Vector2D.unmodifiableVector(new Vector2D(1f, 0f).normalize());
	public static final Vector2D BOTTOM_LEFT = Vector2D.unmodifiableVector(new Vector2D(-1f, -1f).normalize());
	public static final Vector2D BOTTOM = Vector2D.unmodifiableVector(new Vector2D(0f, -1f).normalize());
	public static final Vector2D BOTTOM_RIGHT = Vector2D.unmodifiableVector(new Vector2D(1f, -1f).normalize());
	@Override
	public String toString(){
		return Arrays.toString(data);
	}
	@Override
	public boolean equals(Object object){
		if(object==null){
			return false;
		}
		if(object instanceof Vector){
			Vector vector = (Vector)object;
			if(vector.getDimensions()!=this.getDimensions()){
				return false;
			}
			for(int i=0;i<data.length;++i){
				if(data[i]!=vector.get(i)){
					return false;
				}
			}
			return true;
		}
		return false;
	}
	public static Vector unmodifiableVector(Vector vector){
		return new Vector(vector){
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