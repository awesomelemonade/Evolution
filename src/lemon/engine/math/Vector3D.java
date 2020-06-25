package lemon.engine.math;

public class Vector3D extends Vector<Vector3D> {
	public static final Vector3D[] EMPTY_ARRAY = new Vector3D[] {};
	public static final Vector3D ZERO = Vector.unmodifiableVector(new Vector3D());

	public Vector3D() {
		this(0f, 0f, 0f);
	}
	public Vector3D(float x, float y, float z) {
		super(Vector3D.class, Vector3D::new, x, y, z);
	}
	public Vector3D(Vector3D vector) {
		this(vector.getX(), vector.getY(), vector.getZ());
	}
	public void set(float x, float y, float z) {
		setX(x);
		setY(y);
		setZ(z);
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
	public void setZ(float z) {
		this.set(2, z);
	}
	public float getZ() {
		return this.get(2);
	}
	public Vector3D crossProduct(Vector3D vector) { // Implemented in only Vector3D
		float newX = this.getY() * vector.getZ() - vector.getY() * this.getZ();
		float newY = this.getZ() * vector.getX() - vector.getZ() * this.getX();
		float newZ = this.getX() * vector.getY() - vector.getX() * this.getY();
		this.setX(newX);
		this.setY(newY);
		this.setZ(newZ);
		return this;
	}
}
