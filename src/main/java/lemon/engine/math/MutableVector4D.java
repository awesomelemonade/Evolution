package lemon.engine.math;

public interface MutableVector4D {
	// construction
	public static MutableVector4D ofZero() {
		return of(0f, 0f, 0f, 0f);
	}

	public static MutableVector4D of(Vector4D vector) {
		return of(vector.x(), vector.y(), vector.z(), vector.w());
	}

	public static MutableVector4D of(float initialX, float initialY, float initialZ, float initialW) {
		return new MutableVector4D() {
			private float x = initialX;
			private float y = initialY;
			private float z = initialZ;
			private float w = initialW;
			private final Vector4D immutable = new Vector4D() {
				@Override
				public float x() {
					return x;
				}

				@Override
				public float y() {
					return y;
				}

				@Override
				public float z() {
					return z;
				}

				@Override
				public float w() {
					return w;
				}
			};

			@Override
			public MutableVector4D setX(float x) {
				this.x = x;
				return this;
			}

			@Override
			public MutableVector4D setY(float y) {
				this.y = y;
				return this;
			}

			@Override
			public MutableVector4D setZ(float z) {
				this.z = z;
				return this;
			}

			@Override
			public MutableVector4D setW(float w) {
				this.w = w;
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
			public float z() {
				return z;
			}

			@Override
			public float w() {
				return w;
			}

			@Override
			public Vector4D asImmutable() {
				return immutable;
			}

			@Override
			public String toString() {
				return String.format("MutableVector4D[x=%f, y=%f, z=%f, w=%f]", x, y, z, w);
			}
		};
	}

	// standard
	public MutableVector4D setX(float x);

	public MutableVector4D setY(float y);

	public MutableVector4D setZ(float z);

	public MutableVector4D setW(float w);

	public float x();

	public float y();

	public float z();

	public float w();

	public Vector4D asImmutable();

	// default operations

	public default MutableVector4D addX(float x) {
		setX(x() + x);
		return this;
	}

	public default MutableVector4D addY(float y) {
		setY(y() + y);
		return this;
	}

	public default MutableVector4D addZ(float z) {
		setZ(z() + z);
		return this;
	}

	public default MutableVector4D addW(float w) {
		setW(w() + w);
		return this;
	}

	public default MutableVector4D subtractX(float x) {
		setX(x() - x);
		return this;
	}

	public default MutableVector4D subtractY(float y) {
		setY(y() - y);
		return this;
	}

	public default MutableVector4D subtractZ(float z) {
		setZ(z() - z);
		return this;
	}

	public default MutableVector4D subtractW(float w) {
		setW(w() - w);
		return this;
	}

	public default MutableVector4D multiply(float scale) {
		setX(x() * scale);
		setY(y() * scale);
		setZ(z() * scale);
		setW(w() * scale);
		return this;
	}

	public default MutableVector4D divide(float scale) {
		setX(x() / scale);
		setY(y() / scale);
		setZ(z() / scale);
		setW(w() / scale);
		return this;
	}

	// vector operations
	public default MutableVector4D set(float x, float y, float z, float w) {
		setX(x);
		setY(y);
		setZ(z);
		setW(w);
		return this;
	}

	public default MutableVector4D set(Vector4D vector) {
		return set(vector.x(), vector.y(), vector.z(), vector.w());
	}

	public default MutableVector4D add(float x, float y, float z, float w) {
		addX(x);
		addY(y);
		addZ(z);
		addW(w);
		return this;
	}

	public default MutableVector4D add(Vector4D vector) {
		return add(vector.x(), vector.y(), vector.z(), vector.w());
	}
}
