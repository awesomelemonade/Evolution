package lemon.engine.math;

import java.util.function.Consumer;
import java.util.function.Supplier;

public interface MutableVector3D {
	// construction
	public static MutableVector3D ofZero() {
		return of(0, 0, 0);
	}

	public static MutableVector3D of(Vector3D vector) {
		return of(vector.x(), vector.y(), vector.z());
	}

	public static MutableVector3D of(float x, float y, float z) {
		return new Impl(x, y, z);
	}

	public static MutableVector3D of(Consumer<Float> setX, Consumer<Float> setY, Consumer<Float> setZ,
									 Supplier<Float> getX, Supplier<Float> getY, Supplier<Float> getZ) {
		return new MutableVector3D() {
			private final Vector3D immutable = new Vector3D() {
				@Override
				public float x() {
					return getX.get();
				}

				@Override
				public float y() {
					return getY.get();
				}

				@Override
				public float z() {
					return getZ.get();
				}
			};

			@Override
			public MutableVector3D setX(float x) {
				setX.accept(x);
				return this;
			}

			@Override
			public MutableVector3D setY(float y) {
				setY.accept(y);
				return this;
			}

			@Override
			public MutableVector3D setZ(float z) {
				setZ.accept(z);
				return this;
			}

			@Override
			public float x() {
				return getX.get();
			}

			@Override
			public float y() {
				return getY.get();
			}

			@Override
			public float z() {
				return getZ.get();
			}

			@Override
			public Vector3D asImmutable() {
				return immutable;
			}
		};
	}

	public class Impl implements MutableVector3D {
		private float x;
		private float y;
		private float z;
		private final Vector3D immutable = new Vector3D() {
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
			public String toString() {
				return Vector3D.toString(this);
			}
		};

		public Impl(float x, float y, float z) {
			this.x = x;
			this.y = y;
			this.z = z;
		}

		@Override
		public MutableVector3D setX(float x) {
			this.x = x;
			return this;
		}

		@Override
		public MutableVector3D setY(float y) {
			this.y = y;
			return this;
		}

		@Override
		public MutableVector3D setZ(float z) {
			this.z = z;
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
		public Vector3D asImmutable() {
			return immutable;
		}

		@Override
		public String toString() {
			return String.format("MutableVector3D[x=%f, y=%f, z=%f]", x, y, z);
		}
	}

	// standard
	public MutableVector3D setX(float x);

	public MutableVector3D setY(float y);

	public MutableVector3D setZ(float z);

	public float x();

	public float y();

	public float z();

	public Vector3D asImmutable();

	// default operations

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

	public default MutableVector3D multiply(Vector3D scale) {
		setX(x() * scale.x());
		setY(y() * scale.y());
		setZ(z() * scale.z());
		return this;
	}

	public default MutableVector3D divide(float scale) {
		setX(x() / scale);
		setY(y() / scale);
		setZ(z() / scale);
		return this;
	}

	public default MutableVector3D divide(Vector3D scale) {
		setX(x() / scale.x());
		setY(y() / scale.y());
		setZ(z() / scale.z());
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
	public default MutableVector2D asXYVector() {
		return MutableVector2D.of(this::setX, this::setY, this::x, this::y);
	}
	public default MutableVector2D asXZVector() {
		return MutableVector2D.of(this::setX, this::setZ, this::x, this::z);
	}
}
