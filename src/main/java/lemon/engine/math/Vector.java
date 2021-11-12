package lemon.engine.math;

import com.google.errorprone.annotations.CheckReturnValue;

import java.util.function.UnaryOperator;

public interface Vector<T extends Vector<T>> extends FloatData {
	@CheckReturnValue
	public T operate(UnaryOperator<Float> operator);

	@CheckReturnValue
	public T add(T vector);

	@CheckReturnValue
	public T subtract(T vector);

	@CheckReturnValue
	public T multiply(T vector);

	@CheckReturnValue
	public T multiply(float scale);

	@CheckReturnValue
	public T divide(T vector);

	@CheckReturnValue
	public T divide(float scale);

	@CheckReturnValue
	public float lengthSquared();

	@CheckReturnValue
	public float dotProduct(T vector);

	@CheckReturnValue
	public default float length() {
		return (float) Math.sqrt(lengthSquared());
	}

	@CheckReturnValue
	public default float distanceSquared(T vector) {
		return this.subtract(vector).lengthSquared();
	}

	@CheckReturnValue
	public default boolean isWithinDistanceSquared(T vector, float radius) {
		return distanceSquared(vector) <= radius * radius;
	}

	@CheckReturnValue
	public default float distance(T vector) {
		return (float) Math.sqrt(distanceSquared(vector));
	}

	@CheckReturnValue
	public default T invert() {
		return this.multiply(-1);
	}

	@CheckReturnValue
	public default T normalize() {
		return this.divide(this.length());
	}

	@CheckReturnValue
	public default T scaleToLength(float length) {
		float currentLength = this.length();
		if (currentLength == 0f) {
			throw new IllegalStateException("Cannot scale a vector with length 0");
		}
		return this.multiply(length / currentLength);
	}

	@CheckReturnValue
	public default T average(T vector) {
		return this.add(vector).divide(2);
	}

	@CheckReturnValue
	public default boolean hasNaN() {
		return Float.isNaN(lengthSquared());
	}
}
