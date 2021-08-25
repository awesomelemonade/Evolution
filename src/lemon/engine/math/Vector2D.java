package lemon.engine.math;

import lemon.engine.toolbox.Lazy;

public record Vector2D(float x, float y, Lazy<float[]> dataArray) implements VectorData {
	public static final Vector2D ZERO = new Vector2D(0, 0);

	public Vector2D(float x, float y) {
		this(x, y, Lazy.of(() -> new float[] {x, y}));
	}
	public Vector2D(Vector2D vector) {
		this(vector.x, vector.y, vector.dataArray);
	}

	public Vector2D add(Vector2D vector) {
		return new Vector2D(x + vector.x, y + vector.y);
	}
	public Vector2D subtract(Vector2D vector) {
		return new Vector2D(x - vector.x, y - vector.y);
	}
	public Vector2D multiply(Vector2D vector) {
		return new Vector2D(x * vector.x, y * vector.y);
	}
	public Vector2D multiply(float scale) {
		return new Vector2D(x * scale, y * scale);
	}
	public Vector2D divide(Vector2D vector) {
		return new Vector2D(x / vector.x, y / vector.y);
	}
	public Vector2D divide(float scale) {
		return new Vector2D(x / scale, y / scale);
	}
	public float lengthSquared() {
		return x * x + y * y;
	}
	public float length() {
		return (float) Math.sqrt(lengthSquared());
	}
	public float distanceSquared(float x, float y) {
		float dx = this.x - x;
		float dy = this.y - y;
		return dx * dx + dy * dy;
	}
	public float distanceSquared(Vector2D vector) {
		return distanceSquared(vector.x, vector.y);
	}
	public float distance(Vector2D vector) {
		return (float) Math.sqrt(distanceSquared(vector));
	}
	public Vector2D invert() {
		return new Vector2D(-x, -y);
	}
	public Vector2D normalize() {
		return this.divide(this.length());
	}
	public Vector2D scaleToLength(float length) {
		return this.multiply(length / this.length());
	}
	public float dotProduct(Vector2D vector) {
		return x * vector.x + y * vector.y;
	}
}
