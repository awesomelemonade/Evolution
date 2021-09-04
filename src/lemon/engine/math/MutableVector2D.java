package lemon.engine.math;

public interface MutableVector2D {
	// construction
	public static MutableVector2D of(Vector2D vector) {
		return of(vector.x(), vector.y());
	}

	public static MutableVector2D of(float initialX, float initialY) {
		return new MutableVector2D() {
			private float x = initialX;
			private float y = initialY;
			private final Vector2D immutable = new Vector2D() {
				@Override
				public float x() {
					return x;
				}

				@Override
				public float y() {
					return y;
				}
			};

			@Override
			public MutableVector2D setX(float x) {
				this.x = x;
				return this;
			}

			@Override
			public MutableVector2D setY(float y) {
				this.y = y;
				return this;
			}

			@Override
			public float x() {
				return x;
			}

			@Override
			public float y() {
				return y;
			}

			@Override
			public Vector2D asImmutable() {
				return immutable;
			}

			@Override
			public String toString() {
				return String.format("MutableVector2D[x=%f, y=%f]", x, y);
			}
		};
	}

	// standard
	public MutableVector2D setX(float x);

	public MutableVector2D setY(float y);

	public float x();

	public float y();

	public Vector2D asImmutable();

	// default operations

	public default MutableVector2D addX(float x) {
		setX(x() + x);
		return this;
	}

	public default MutableVector2D addY(float y) {
		setY(y() + y);
		return this;
	}

	public default MutableVector2D subtractX(float x) {
		setX(x() - x);
		return this;
	}

	public default MutableVector2D subtractY(float y) {
		setY(y() - y);
		return this;
	}

	public default MutableVector2D multiply(float scale) {
		setX(x() * scale);
		setY(y() * scale);
		return this;
	}

	public default MutableVector2D divide(float scale) {
		setX(x() / scale);
		setY(y() / scale);
		return this;
	}

	// extended operations
	public default MutableVector2D clampX(float low, float high) {
		setX(MathUtil.clamp(x(), low, high));
		return this;
	}

	public default MutableVector2D clampY(float low, float high) {
		setY(MathUtil.clamp(y(), low, high));
		return this;
	}

	public default MutableVector2D modX(float mod) {
		setX(MathUtil.mod(x(), mod));
		return this;
	}

	public default MutableVector2D modY(float mod) {
		setY(MathUtil.mod(y(), mod));
		return this;
	}

	// vector operations
	public default MutableVector2D set(float x, float y) {
		setX(x);
		setY(y);
		return this;
	}

	public default MutableVector2D set(Vector2D vector) {
		return set(vector.x(), vector.y());
	}

	public default MutableVector2D add(float x, float y) {
		addX(x);
		addY(y);
		return this;
	}

	public default MutableVector2D add(Vector2D vector) {
		return add(vector.x(), vector.y());
	}

	public default MutableVector2D subtract(Vector2D vector) {
		subtractX(vector.x());
		subtractY(vector.y());
		return this;
	}
}
