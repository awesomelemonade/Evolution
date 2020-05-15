package lemon.engine.math;

public class Vector2D extends Vector<Vector2D> {
	public static final Vector2D[] EMPTY_ARRAY = new Vector2D[] {};
	public static final Vector2D ZERO = Vector.unmodifiableVector(new Vector2D());
	public static final Vector2D TOP_LEFT = Vector.unmodifiableVector(new Vector2D(-1f, 1f).normalize());
	public static final Vector2D TOP = Vector.unmodifiableVector(new Vector2D(0f, 1f).normalize());
	public static final Vector2D TOP_RIGHT = Vector.unmodifiableVector(new Vector2D(1f, 1f).normalize());
	public static final Vector2D LEFT = Vector.unmodifiableVector(new Vector2D(-1f, 0f).normalize());
	public static final Vector2D RIGHT = Vector.unmodifiableVector(new Vector2D(1f, 0f).normalize());
	public static final Vector2D BOTTOM_LEFT = Vector.unmodifiableVector(new Vector2D(-1f, -1f).normalize());
	public static final Vector2D BOTTOM = Vector.unmodifiableVector(new Vector2D(0f, -1f).normalize());
	public static final Vector2D BOTTOM_RIGHT = Vector.unmodifiableVector(new Vector2D(1f, -1f).normalize());

	public Vector2D() {
		this(0, 0);
	}
	public Vector2D(float x, float y) {
		super(Vector2D.class, x, y);
	}
	public Vector2D(Vector2D vector) {
		this(vector.getX(), vector.getY());
	}
	public void setX(float x) {
		this.set(0, x);
	}
	public float getX() {
		return this.get(0);
	}
	public void setY(float y) {
		this.set(1, y);
	}
	public float getY() {
		return this.get(1);
	}
}
