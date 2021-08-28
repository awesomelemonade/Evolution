package lemon.engine.math;

import java.util.function.UnaryOperator;

public interface Vector<T extends Vector<T>> extends HasDataArray {
	public T operate(UnaryOperator<Float> operator);

	public T add(T vector);

	public T subtract(T vector);

	public T multiply(T vector);

	public T multiply(float scale);

	public T divide(T vector);

	public T divide(float scale);

	public float lengthSquared();

	public float dotProduct(T vector);

	public default float length() {
		return (float) Math.sqrt(lengthSquared());
	}

	public default float distanceSquared(T vector) {
		return this.subtract(vector).lengthSquared();
	}

	public default float distance(T vector) {
		return (float) Math.sqrt(distanceSquared(vector));
	}

	public default T invert() {
		return this.multiply(-1);
	}

	public default T normalize() {
		return this.divide(this.length());
	}

	public default T scaleToLength(float length) {
		return this.multiply(length / this.length());
	}

	public default T average(T vector) {
		return this.add(vector).divide(2);
	}
}
