package lemon.engine.math;

import lemon.engine.toolbox.Cache;

public interface MutableVector3D {
	// construction
	public static MutableVector3D ofZero() {
		return of(0, 0, 0);
	}

	public static MutableVector3D of(Vector3D vector) {
		return of(vector.x(), vector.y(), vector.z());
	}

	public static MutableVector3D of(float initialX, float initialY, float initialZ) {
		return new MutableVector3D() { // Does this create a new class every time?
			private float x = initialX;
			private float y = initialY;
			private float z = initialZ;
			private final Cache<Vector3D> cache = new Cache<>(() -> new Vector3D(x, y, z));

			@Override
			public MutableVector3D setX(float x) {
				this.x = x;
				cache.invalidate();
				return this;
			}

			@Override
			public MutableVector3D setY(float y) {
				this.y = y;
				cache.invalidate();
				return this;
			}

			@Override
			public MutableVector3D setZ(float z) {
				this.z = z;
				cache.invalidate();
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
			public Vector3D toImmutable() {
				return cache.get();
			}
		};
	}

	// standard
	public MutableVector3D setX(float x);

	public MutableVector3D setY(float y);

	public MutableVector3D setZ(float z);

	public float x();

	public float y();

	public float z();

	// default operations
	public default Vector3D toImmutable() {
		return new Vector3D(x(), y(), z());
	}

	public default MutableVector3D addX(float x) {
		setX(x() + x);
		return this;
	}

	public default MutableVector3D addY(float y) {
		setY(y() + y);
		return this;
	}

	public default MutableVector3D addZ(float z) {
		setZ(z() + z);
		return this;
	}

	public default MutableVector3D subtractX(float x) {
		setX(x() - x);
		return this;
	}

	public default MutableVector3D subtractY(float y) {
		setY(y() - y);
		return this;
	}

	public default MutableVector3D subtractZ(float z) {
		setZ(z() - z);
		return this;
	}

	public default MutableVector3D multiply(float scale) {
		setX(x() * scale);
		setY(y() * scale);
		setZ(z() * scale);
		return this;
	}

	public default MutableVector3D divide(float scale) {
		setX(x() / scale);
		setY(y() / scale);
		setZ(z() / scale);
		return this;
	}

	// vector operations
	public default MutableVector3D set(float x, float y, float z) {
		setX(x);
		setY(y);
		setZ(z);
		return this;
	}

	public default MutableVector3D set(Vector3D vector) {
		return set(vector.x(), vector.y(), vector.z());
	}

	public default MutableVector3D add(float x, float y, float z) {
		addX(x);
		addY(y);
		addZ(z);
		return this;
	}

	public default MutableVector3D add(Vector3D vector) {
		return add(vector.x(), vector.y(), vector.z());
	}

	public default MutableVector3D subtract(Vector3D vector) {
		subtractX(vector.x());
		subtractY(vector.y());
		subtractZ(vector.z());
		return this;
	}

	// derivations
	public default MutableVector2D asXYVector() { // Could potentially be lazy
		return new MutableVector2D() {
			@Override
			public MutableVector2D setX(float x) {
				MutableVector3D.this.setX(x);
				return this;
			}

			@Override
			public MutableVector2D setY(float y) {
				MutableVector3D.this.setY(y);
				return this;
			}

			@Override
			public float x() {
				return MutableVector3D.this.x();
			}

			@Override
			public float y() {
				return MutableVector3D.this.y();
			}
		};
	}

	public default MutableVector2D asXZVector() { // Could potentially be lazy
		return new MutableVector2D() {
			@Override
			public MutableVector2D setX(float x) {
				MutableVector3D.this.setX(x);
				return this;
			}

			@Override
			public MutableVector2D setY(float y) {
				MutableVector3D.this.setZ(y);
				return this;
			}

			@Override
			public float x() {
				return MutableVector3D.this.x();
			}

			@Override
			public float y() {
				return MutableVector3D.this.z();
			}
		};
	}
}
