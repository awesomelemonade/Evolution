package lemon.engine.math;

import lemon.engine.toolbox.Lazy;

import java.util.Arrays;
import java.util.function.UnaryOperator;

public record Vector3D(float x, float y, float z, Lazy<float[]> dataArray) implements Vector<Vector3D> {
	public static final Vector3D ZERO = new Vector3D(0, 0, 0);
	public static final Vector3D[] EMPTY_ARRAY = new Vector3D[] {};

	public Vector3D(float x, float y, float z) {
		this(x, y, z, new Lazy<>(() -> new float[] {x, y, z}));
	}

	public Vector3D(Vector3D vector) {
		this(vector.x, vector.y, vector.z, vector.dataArray);
	}

	public Vector3D crossProduct(Vector3D vector) { // Implemented in only Vector3D
		float newX = this.y() * vector.z() - vector.y() * this.z();
		float newY = this.z() * vector.x() - vector.z() * this.x();
		float newZ = this.x() * vector.y() - vector.x() * this.y();
		return new Vector3D(newX, newY, newZ);
	}

	@Override
	public Vector3D operate(UnaryOperator<Float> operator) {
		return new Vector3D(operator.apply(x), operator.apply(y), operator.apply(z));
	}

	@Override
	public Vector3D add(Vector3D vector) {
		return new Vector3D(x + vector.x, y + vector.y, z + vector.z);
	}

	@Override
	public Vector3D subtract(Vector3D vector) {
		return new Vector3D(x - vector.x, y - vector.y, z - vector.z);
	}

	@Override
	public Vector3D multiply(Vector3D vector) {
		return new Vector3D(x * vector.x, y * vector.y, z * vector.z);
	}

	@Override
	public Vector3D multiply(float scale) {
		return new Vector3D(x * scale, y * scale, z * scale);
	}

	@Override
	public Vector3D divide(Vector3D vector) {
		return new Vector3D(x / vector.x, y / vector.y, z / vector.z);
	}

	@Override
	public Vector3D divide(float scale) {
		return new Vector3D(x / scale, y / scale, z / scale);
	}

	@Override
	public float lengthSquared() {
		return x * x + y * y + z * z;
	}

	@Override
	public float dotProduct(Vector3D vector) {
		return x * vector.x + y * vector.y + z * vector.z;
	}

	public float distanceSquared(float x, float y, float z) {
		float dx = this.x - x;
		float dy = this.y - y;
		float dz = this.z - z;
		return dx * dx + dy * dy + dz * dz;
	}

	public Vector2D toXYVector() {
		return new Vector2D(x, y);
	}

	public Vector2D toXZVector() {
		return new Vector2D(x, z);
	}

	@Override
	public int hashCode() {
		return Arrays.hashCode(this.constantData());
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		Vector3D vector = (Vector3D) o;
		return x == vector.x && y == vector.y && z == vector.z;
	}

	@Override
	public String toString() {
		return String.format("Vector3D[x=%f, y=%f, z=%f]", x, y, z);
	}
}
