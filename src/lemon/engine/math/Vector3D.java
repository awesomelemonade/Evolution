package lemon.engine.math;

import lemon.engine.toolbox.Lazy;

public record Vector3D(float x, float y, float z, Lazy<float[]> dataArray) implements VectorData {
	public static final Vector3D ZERO = new Vector3D(0, 0, 0);
	public static final Vector3D[] EMPTY_ARRAY = new Vector3D[] {};

	public Vector3D(float x, float y, float z) {
		this(x, y, z, Lazy.of(() -> new float[] {x, y, z}));
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
	public Vector3D add(Vector3D vector) {
		return new Vector3D(x + vector.x, y + vector.y, z + vector.z);
	}
	public Vector3D subtract(Vector3D vector) {
		return new Vector3D(x - vector.x, y - vector.y, z - vector.z);
	}
	public Vector3D multiply(Vector3D vector) {
		return new Vector3D(x * vector.x, y * vector.y, z * vector.z);
	}
	public Vector3D multiply(float scale) {
		return new Vector3D(x * scale, y * scale, z * scale);
	}
	public Vector3D divide(Vector3D vector) {
		return new Vector3D(x / vector.x, y / vector.y, z / vector.z);
	}
	public Vector3D divide(float scale) {
		return new Vector3D(x / scale, y / scale, z / scale);
	}
	public float lengthSquared() {
		return x * x + y * y + z * z;
	}
	public float length() {
		return (float) Math.sqrt(lengthSquared());
	}
	public float distanceSquared(Vector3D vector) {
		return this.subtract(vector).lengthSquared();
	}
	public float distance(Vector3D vector) {
		return (float) Math.sqrt(distanceSquared(vector));
	}
	public Vector3D invert() {
		return new Vector3D(-x, -y, -z);
	}
	public Vector3D normalize() {
		return this.divide(this.length());
	}
	public Vector3D scaleToLength(float length) {
		return this.multiply(length / this.length());
	}
	public float dotProduct(Vector3D vector) {
		return x * vector.x + y * vector.y + z * vector.z;
	}
	public Vector2D toXYVector() {
		return new Vector2D(x, y);
	}
	public Vector2D toXZVector() {
		return new Vector2D(x, z);
	}
}
