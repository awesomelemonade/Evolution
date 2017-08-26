package lemon.engine.math;

import java.util.Arrays;
import java.util.function.BinaryOperator;
import java.util.function.Function;

public class Vector {
	public static final Function<float[], Vector> supplier = (data) -> new Vector(data);
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
	private void checkDimensions(float[] data){
		if(this.getDimensions()!=data.length){
			throw new IllegalArgumentException("Dimensions are not equal");
		}
	}
	public void set(Vector vector){
		checkDimensions(vector);
		for(int i=0;i<data.length;++i){
			data[i] = vector.get(i);
		}
	}
	public void set(float[] data){
		checkDimensions(data);
		this.data = data;
	}
	public void set(int index, float data){
		this.data[index] = data;
	}
	public float get(int index){
		return data[index];
	}
	public Vector normalize(){
		return normalize(supplier);
	}
	public <T extends Vector> T normalize(Function<float[], T> supplier){
		float magnitude = 0;
		for(int i=0;i<data.length;++i){
			magnitude+=Math.pow(data[i], 2);
		}
		magnitude = (float)Math.sqrt(magnitude);
		float[] data = new float[this.data.length];
		for(int i=0;i<data.length;++i){
			data[i] = this.data[i]/magnitude;
		}
		return supplier.apply(data);
	}
	public Vector getInvert(){
		return getInvert(supplier);
	}
	public <T extends Vector> T getInvert(Function<float[], T> supplier){
		float[] data = new float[this.data.length];
		for(int i=0;i<data.length;++i){
			data[i] = -this.data[i];
		}
		return supplier.apply(data);
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
		return add(vector, supplier);
	}
	public Vector subtract(Vector vector){
		return subtract(vector, supplier);
	}
	public Vector multiply(Vector vector){
		return multiply(vector, supplier);
	}
	public Vector multiply(float scale){
		return multiply(scale, supplier);
	}
	public Vector divide(Vector vector){
		return divide(vector, supplier);
	}
	public Vector divide(float scale){
		return divide(scale, supplier);
	}
	public Vector average(Vector vector){
		return average(vector, supplier);
	}
	public <T extends Vector> T add(T vector, Function<float[], T> supplier){
		return operate(vector, BasicFloatOperator.ADDITION, supplier);
	}
	public <T extends Vector> T subtract(T vector, Function<float[], T> supplier){
		return operate(vector, BasicFloatOperator.SUBTRACTION, supplier);
	}
	public <T extends Vector> T multiply(T vector, Function<float[], T> supplier){
		return operate(vector, BasicFloatOperator.MULTIPLICATION, supplier);
	}
	public <T extends Vector> T multiply(float scale, Function<float[], T> supplier){
		return operate(scale, BasicFloatOperator.MULTIPLICATION, supplier);
	}
	public <T extends Vector> T divide(T vector, Function<float[], T> supplier){
		return operate(vector, BasicFloatOperator.DIVISION, supplier);
	}
	public <T extends Vector> T divide(float scale, Function<float[], T> supplier){
		return operate(scale, BasicFloatOperator.DIVISION, supplier);
	}
	public <T extends Vector> T average(T vector, Function<float[], T> supplier){
		return operate(vector, BasicFloatOperator.AVERAGE, supplier);
	}
	public <T extends Vector> T operate(float scale, BinaryOperator<Float> operator, Function<float[], T> supplier){
		float[] data = new float[this.data.length];
		for(int i=0;i<data.length;++i){
			data[i] = operator.apply(this.data[i], scale);
		}
		return supplier.apply(data);
	}
	public <T extends Vector> T operate(T vector, BinaryOperator<Float> operator, Function<float[], T> supplier){
		checkDimensions(vector);
		float[] data = new float[this.data.length];
		for(int i=0;i<data.length;++i){
			data[i] = operator.apply(this.data[i], vector.get(i));
		}
		return supplier.apply(data);
	}
	public float dotProduct(Vector vector){
		checkDimensions(vector);
		float sum = 0;
		for(int i=0;i<data.length;++i){
			sum+=data[i]*vector.get(i);
		}
		return sum;
	}
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