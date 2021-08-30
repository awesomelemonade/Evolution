package lemon.engine.math;

import lemon.engine.toolbox.Lazy;

import java.util.Arrays;
import java.util.function.UnaryOperator;

public record Vector2D(float x, float y, Lazy<float[]> dataArray) implements Vector<Vector2D> {
	public static final Vector2D ZERO = new Vector2D(0, 0);

	public Vector2D(float x, float y) {
		this(x, y, new Lazy<>(() -> new float[] {x, y}));
	}

	public Vector2D(Vector2D vector) {
		this(vector.x, vector.y, vector.dataArray);
	}

	@Override
	public Vector2D operate(UnaryOperator<Float> operator) {
		return new Vector2D(operator.apply(x), operator.apply(y));
	}

	@Override
	public Vector2D add(Vector2D vector) {
		return new Vector2D(x + vector.x, y + vector.y);
	}

	@Override
	public Vector2D subtract(Vector2D vector) {
		return new Vector2D(x - vector.x, y - vector.y);
	}

	@Override
	public Vector2D multiply(Vector2D vector) {
		return new Vector2D(x * vector.x, y * vector.y);
	}

	@Override
	public Vector2D multiply(float scale) {
		return new Vector2D(x * scale, y * scale);
	}

	@Override
	public Vector2D divide(Vector2D vector) {
		return new Vector2D(x / vector.x, y / vector.y);
	}

	@Override
	public Vector2D divide(float scale) {
		return new Vector2D(x / scale, y / scale);
	}

	@Override
	public float lengthSquared() {
		return x * x + y * y;
	}

	@Override
	public float dotProduct(Vector2D vector) {
		return x * vector.x + y * vector.y;
	}

	public float distanceSquared(float x, float y) {
		float dx = this.x - x;
		float dy = this.y - y;
		return dx * dx + dy * dy;
	}

	@Override
	public int hashCode() {
		return Arrays.hashCode(this.constantData());
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		Vector2D vector = (Vector2D) o;
		return x == vector.x && y == vector.y;
	}

	@Override
	public String toString() {
		return String.format("Vector2D[x=%f, y=%f]", x, y);
	}
}
