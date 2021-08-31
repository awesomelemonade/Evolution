package lemon.engine.math;

import java.nio.FloatBuffer;
import java.util.function.UnaryOperator;

public interface Vector2D extends Vector<Vector2D> {
	public static final int NUM_DIMENSIONS = 2;
	public static final Vector2D ZERO = Vector2D.of(0, 0);
	
	public float x();
	public float y();

	public static Vector2D of(float x, float y) {
		return new Impl(x, y);
	}

	public static Vector2D ofCopy(Vector2D vector) {
		return new Impl(vector);
	}
	
	record Impl(float x, float y) implements Vector2D {
		public Impl(Vector2D vector) {
			this(vector.x(), vector.y());
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
	}

	@Override
	public default void putInArray(float[] array) {
		array[0] = x();
		array[1] = y();
	}

	@Override
	public default Vector2D operate(UnaryOperator<Float> operator) {
		return Vector2D.of(operator.apply(x()), operator.apply(y()));
	}

	@Override
	public default Vector2D add(Vector2D vector) {
		return Vector2D.of(x() + vector.x(), y() + vector.y());
	}

	@Override
	public default Vector2D subtract(Vector2D vector) {
		return Vector2D.of(x() - vector.x(), y() - vector.y());
	}

	@Override
	public default Vector2D multiply(Vector2D vector) {
		return Vector2D.of(x() * vector.x(), y() * vector.y());
	}

	@Override
	public default Vector2D multiply(float scale) {
		return Vector2D.of(x() * scale, y() * scale);
	}

	@Override
	public default Vector2D divide(Vector2D vector) {
		return Vector2D.of(x() / vector.x(), y() / vector.y());
	}

	@Override
	public default Vector2D divide(float scale) {
		return Vector2D.of(x() / scale, y() / scale);
	}

	@Override
	public default float lengthSquared() {
		float x = x();
		float y = y();
		return x * x + y * y;
	}

	@Override
	public default float dotProduct(Vector2D vector) {
		return x() * vector.x() + y() * vector.y();
	}

	public default float distanceSquared(float x, float y) {
		float dx = x() - x;
		float dy = y() - y;
		return dx * dx + dy * dy;
	}
}
