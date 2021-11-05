package lemon.engine.math;

import java.nio.FloatBuffer;
import java.util.function.UnaryOperator;

public interface Vector4D extends Vector<Vector4D> {
	public static final int NUM_DIMENSIONS = 4;
	public static final Vector4D ZERO = Vector4D.of(0f, 0f, 0f, 0f);
	public static final Vector4D ONE = Vector4D.of(1f, 1f, 1f, 1f);
	public static final Vector4D[] EMPTY_ARRAY = new Vector4D[] {};

	public float x();
	public float y();
	public float z();
	public float w();

	public default float r() {
		return x();
	}

	public default float g() {
		return y();
	}

	public default float b() {
		return z();
	}

	public default float a() {
		return w();
	}

	public static Vector4D of(float x, float y, float z, float w) {
		return new Impl(x, y, z, w);
	}

	public static Vector4D ofCopy(Vector4D vector) {
		return new Impl(vector);
	}

	public static Vector4D ofParsed(String x, String y, String z, String w) {
		return of(Float.parseFloat(x), Float.parseFloat(y), Float.parseFloat(z), Float.parseFloat(w));
	}

	record Impl(float x, float y, float z, float w) implements Vector4D {
		public Impl(Vector4D vector) {
			this(vector.x(), vector.y(), vector.z(), vector.w());
		}

		@Override
		public String toString() {
			return Vector4D.toString(this);
		}
	}

	@Override
	public default int numDimensions() {
		return NUM_DIMENSIONS;
	}

	@Override
	public default void putInBuffer(FloatBuffer buffer) {
		buffer.put(x());
		buffer.put(y());
		buffer.put(z());
		buffer.put(w());
	}

	@Override
	public default void putInArray(float[] array) {
		array[0] = x();
		array[1] = y();
		array[2] = z();
		array[3] = w();
	}

	@Override
	public default Vector4D operate(UnaryOperator<Float> operator) {
		return Vector4D.of(operator.apply(x()), operator.apply(y()), operator.apply(z()), operator.apply(w()));
	}

	@Override
	public default Vector4D add(Vector4D vector) {
		return Vector4D.of(x() + vector.x(), y() + vector.y(), z() + vector.z(), w() + vector.w());
	}

	@Override
	public default Vector4D subtract(Vector4D vector) {
		return Vector4D.of(x() - vector.x(), y() - vector.y(), z() - vector.z(), w() - vector.w());
	}

	@Override
	public default Vector4D multiply(Vector4D vector) {
		return Vector4D.of(x() * vector.x(), y() * vector.y(), z() * vector.z(), w() * vector.w());
	}

	@Override
	public default Vector4D multiply(float scale) {
		return Vector4D.of(x() * scale, y() * scale, z() * scale, w() * scale);
	}

	@Override
	public default Vector4D divide(Vector4D vector) {
		return Vector4D.of(x() / vector.x(), y() / vector.y(), z() / vector.z(), w() / vector.w());
	}

	@Override
	public default Vector4D divide(float scale) {
		return Vector4D.of(x() / scale, y() / scale, z() / scale, w() / scale);
	}

	@Override
	public default float lengthSquared() {
		float x = x();
		float y = y();
		float z = z();
		return x * x + y * y + z * z;
	}

	@Override
	public default float dotProduct(Vector4D vector) {
		return x() * vector.x() + y() * vector.y() + z() * vector.z();
	}

	public static String toString(Vector4D vector) {
		return String.format("Vector4D[x=%f, y=%f, z=%f, w=%f]", vector.x(), vector.y(), vector.z(), vector.w());
	}
}
