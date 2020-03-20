package lemon.engine.math;

import java.util.function.BinaryOperator;
import java.util.function.Function;

public class Vector2D extends Vector {
	public static final Function<float[], Vector2D> supplier = Vector2D::new;
	public static final Vector2D[] EMPTY_ARRAY = new Vector2D[] {};
	public static final Vector2D ZERO = Vector2D.unmodifiableVector(new Vector2D());
	public static final Vector2D TOP_LEFT = Vector2D.unmodifiableVector(new Vector2D(-1f, 1f).normalize());
	public static final Vector2D TOP = Vector2D.unmodifiableVector(new Vector2D(0f, 1f).normalize());
	public static final Vector2D TOP_RIGHT = Vector2D.unmodifiableVector(new Vector2D(1f, 1f).normalize());
	public static final Vector2D LEFT = Vector2D.unmodifiableVector(new Vector2D(-1f, 0f).normalize());
	public static final Vector2D RIGHT = Vector2D.unmodifiableVector(new Vector2D(1f, 0f).normalize());
	public static final Vector2D BOTTOM_LEFT = Vector2D.unmodifiableVector(new Vector2D(-1f, -1f).normalize());
	public static final Vector2D BOTTOM = Vector2D.unmodifiableVector(new Vector2D(0f, -1f).normalize());
	public static final Vector2D BOTTOM_RIGHT = Vector2D.unmodifiableVector(new Vector2D(1f, -1f).normalize());

	public Vector2D() {
		this(0, 0);
	}
	public Vector2D(float x, float y) {
		super(x, y);
	}
	public Vector2D(float[] data) {
		super(2);
		if (data.length != 2) {
			throw new IllegalArgumentException("Only 2 Dimensions Allowed");
		}
		this.setX(data[0]);
		this.setY(data[1]);
	}
	public Vector2D(Vector vector) {
		super(2);
		if (vector.getDimensions() != 2) {
			throw new IllegalArgumentException("Only 2 Dimensions Allowed");
		}
		this.setX(vector.get(0));
		this.setY(vector.get(1));
	}
	public Vector2D(Vector2D vector) {
		super(vector);
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
	@Override
	public Vector2D normalize() {
		float magnitude = (float) Math.sqrt(Math.pow(this.getX(), 2) + Math.pow(this.getY(), 2));
		return new Vector2D(this.getX() / magnitude, this.getY() / magnitude);
	}
	@Override
	public Vector2D getInvert() {
		return new Vector2D(-this.getX(), -this.getY());
	}
	public Vector2D add(Vector2D vector) {
		return operate(vector, BasicFloatOperator.ADDITION, supplier);
	}
	public Vector2D subtract(Vector2D vector) {
		return operate(vector, BasicFloatOperator.SUBTRACTION, supplier);
	}
	public Vector2D multiply(Vector2D vector) {
		return operate(vector, BasicFloatOperator.MULTIPLICATION, supplier);
	}
	@Override
	public Vector2D multiply(float scale) {
		return operate(scale, BasicFloatOperator.MULTIPLICATION, supplier);
	}
	public Vector2D divide(Vector2D vector) {
		return operate(vector, BasicFloatOperator.DIVISION, supplier);
	}
	@Override
	public Vector2D divide(float scale) {
		return operate(scale, BasicFloatOperator.DIVISION, supplier);
	}
	public Vector2D average(Vector2D vector) {
		return operate(vector, BasicFloatOperator.AVERAGE, supplier);
	}
	@Override
	public Vector2D scaleToLength(float length) {
		return this.scaleToLength(length, supplier);
	}
	public static Vector2D unmodifiableVector(Vector2D vector) {
		return new Vector2D(vector) {
			@Override
			public void set(Vector x) {
				throw new IllegalStateException(unmodifiableMessage);
			}
			@Override
			public void set(float[] data) {
				throw new IllegalStateException(unmodifiableMessage);
			}
			@Override
			public void set(int index, float data) {
				throw new IllegalStateException(unmodifiableMessage);
			}
			@Override
			public void selfOperate(float scale, BinaryOperator<Float> operator) {
				throw new IllegalStateException(unmodifiableMessage);
			}
			@Override
			public void selfOperate(Vector vector, BinaryOperator<Float> operator) {
				throw new IllegalStateException(unmodifiableMessage);
			}
		};
	}
}
