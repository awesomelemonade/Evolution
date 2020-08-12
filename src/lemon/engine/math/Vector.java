package lemon.engine.math;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.UnaryOperator;

public class Vector<T extends Vector<T>> {
	private Class<T> clazz;
	private Function<T, T> copier;
	private T self;
	private float[] data;

	protected Vector(Class<T> clazz, Function<T, T> copier, float... data) {
		this.clazz = clazz;
		this.self = clazz.cast(this);
		this.data = data;
		this.copier = copier;
		if (this.copier == null) {
			try {
				final Constructor<T> constructor = clazz.getDeclaredConstructor(clazz);
				this.copier = (x) -> {
					try {
						return constructor.newInstance(x);
					} catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
						throw new IllegalStateException(e);
					}
				};
			} catch (NoSuchMethodException e) {
				this.copier = (x) -> {
					throw new UnsupportedOperationException(
							String.format("%s does not have a copier", clazz.getName()));
				};
			}
		}
	}
	public int getDimensions() {
		return data.length;
	}
	public void set(Vector vector) {
		for (int i = 0; i < data.length; ++i) {
			data[i] = vector.get(i);
		}
	}
	public void set(float... data) {
		this.data = data;
	}
	public void set(int index, float data) {
		this.data[index] = data;
	}
	public float get(int index) {
		return data[index];
	}
	public T normalize() {
		float magnitude = 0;
		for (int i = 0; i < data.length; i++) {
			magnitude += data[i] * data[i];
		}
		if (magnitude == 0) {
			throw new IllegalStateException("Cannot normalize a 0 magnitude vector");
		}
		magnitude = (float) Math.sqrt(magnitude);
		for (int i = 0; i < data.length; ++i) {
			data[i] /= magnitude;
		}
		return self;
	}
	public T invert() {
		for (int i = 0; i < data.length; i++) {
			data[i] = -data[i];
		}
		return self;
	}
	public float getAbsoluteValue() {
		return (float) Math.sqrt(getAbsoluteValueSquared());
	}
	public float getAbsoluteValueSquared() {
		float sum = 0;
		for (int i = 0; i < data.length; i++) {
			sum += data[i] * data[i];
		}
		return sum;
	}
	public float getLength() {
		return this.getAbsoluteValue();
	}
	public float getLengthSquared() {
		return this.getAbsoluteValueSquared();
	}
	public float getDistance(Vector vector) {
		return (float) Math.sqrt(getDistanceSquared(vector));
	}
	public float getDistanceSquared(Vector vector) {
		float sum = 0;
		for (int i = 0; i < data.length; i++) {
			float delta = vector.get(i) - data[i];
			sum += delta * delta;
		}
		return sum;
	}
	public T add(Vector<T> vector) {
		for (int i = 0; i < data.length; i++) {
			data[i] += vector.get(i);
		}
		return self;
	}
	public T subtract(Vector<T> vector) {
		for (int i = 0; i < data.length; i++) {
			data[i] -= vector.get(i);
		}
		return self;
	}
	public T multiply(Vector<T> vector) {
		for (int i = 0; i < data.length; i++) {
			data[i] *= vector.get(i);
		}
		return self;
	}
	public T divide(Vector<T> vector) {
		for (int i = 0; i < data.length; i++) {
			data[i] /= vector.get(i);
		}
		return self;
	}
	public T multiply(float factor) {
		for (int i = 0; i < data.length; i++) {
			data[i] *= factor;
		}
		return self;
	}
	public T divide(float factor) {
		for (int i = 0; i < data.length; i++) {
			data[i] /= factor;
		}
		return self;
	}
	public T average(Vector<T> vector) {
		for (int i = 0; i < data.length; i++) {
			data[i] = (data[i] + vector.get(i)) / 2;
		}
		return self;
	}
	public T operate(UnaryOperator<Float> operator) {
		for (int i = 0; i < data.length; i++) {
			data[i] = operator.apply(data[i]);
		}
		return self;
	}
	public T operate(Vector<T> vector, BinaryOperator<Float> operator) {
		for (int i = 0; i < data.length; i++) {
			data[i] = operator.apply(data[i], vector.get(i));
		}
		return self;
	}
	public T scaleToLength(float length) {
		return multiply(length / this.getLength());
	}
	public float dotProduct(Vector vector) {
		float sum = 0;
		for (int i = 0; i < data.length; ++i) {
			sum += data[i] * vector.get(i);
		}
		return sum;
	}
	public T copy() {
		return copier.apply(self);
	}
	public static <T extends Vector<T>> T unmodifiableVector(Vector<T> vector) {
		// dummy - does not make it actually unmodifiable
		return vector.clazz.cast(vector);
	}
	public static void invert(Vector result, Vector vector) {
		for (int i = 0; i < vector.getDimensions(); i++) {
			result.set(i, -vector.get(i));
		}
	}
	@Override
	public String toString() {
		return Arrays.toString(data);
	}
	@Override
	public boolean equals(Object object) {
		if (object == null) {
			return false;
		}
		if (object instanceof Vector) {
			Vector vector = (Vector) object;
			if (vector.getDimensions() != this.getDimensions()) {
				return false;
			}
			for (int i = 0; i < data.length; ++i) {
				if (data[i] != vector.get(i)) {
					return false;
				}
			}
			return true;
		}
		return false;
	}
	@Override
	public int hashCode() {
		return Arrays.hashCode(data);
	}
}