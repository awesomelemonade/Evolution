package lemon.engine.math;

public interface MutableQuaternion {
	// construction
	public static MutableQuaternion ofZero() {
		return of(0f, 0f, 0f, 0f);
	}

	public static MutableQuaternion of(Quaternion quaternion) {
		return of(quaternion.a(), quaternion.b(), quaternion.c(), quaternion.d());
	}

	public static MutableQuaternion of(float initialA, float initialB, float initialC, float initialD) {
		return new MutableQuaternion() {
			private float a = initialA;
			private float b = initialB;
			private float c = initialC;
			private float d = initialD;
			private final Quaternion immutable = new Quaternion() {
				@Override
				public float a() {
					return a;
				}

				@Override
				public float b() {
					return b;
				}

				@Override
				public float c() {
					return c;
				}

				@Override
				public float d() {
					return d;
				}
			};

			@Override
			public MutableQuaternion setA(float x) {
				this.a = x;
				return this;
			}

			@Override
			public MutableQuaternion setB(float y) {
				this.b = y;
				return this;
			}

			@Override
			public MutableQuaternion setC(float z) {
				this.c = z;
				return this;
			}

			@Override
			public MutableQuaternion setD(float w) {
				this.d = w;
				return this;
			}

			@Override
			public float a() {
				return a;
			}

			@Override
			public float b() {
				return b;
			}

			@Override
			public float c() {
				return c;
			}

			@Override
			public float d() {
				return d;
			}

			@Override
			public Quaternion asImmutable() {
				return immutable;
			}

			@Override
			public String toString() {
				return String.format("MutableVector4D[x=%f, y=%f, z=%f, w=%f]", a, b, c, d);
			}
		};
	}

	// standard
	public MutableQuaternion setA(float a);

	public MutableQuaternion setB(float b);

	public MutableQuaternion setC(float c);

	public MutableQuaternion setD(float d);

	public float a();

	public float b();

	public float c();

	public float d();

	public Quaternion asImmutable();

	// default operations

	public default MutableQuaternion addA(float a) {
		setA(a() + a);
		return this;
	}

	public default MutableQuaternion addB(float b) {
		setB(b() + b);
		return this;
	}

	public default MutableQuaternion addC(float c) {
		setC(c() + c);
		return this;
	}

	public default MutableQuaternion addD(float d) {
		setD(d() + d);
		return this;
	}

	public default MutableQuaternion multiply(float scale) {
		setA(a() * scale);
		setB(b() * scale);
		setC(c() * scale);
		setD(d() * scale);
		return this;
	}

	public default MutableQuaternion divide(float scale) {
		setA(a() / scale);
		setB(b() / scale);
		setC(c() / scale);
		setD(d() / scale);
		return this;
	}

	// vector operations
	public default MutableQuaternion set(float a, float b, float c, float d) {
		setA(a);
		setB(b);
		setC(c);
		setD(d);
		return this;
	}

	public default MutableQuaternion set(Quaternion quaternion) {
		return set(quaternion.a(), quaternion.b(), quaternion.c(), quaternion.d());
	}

	public default MutableQuaternion add(float a, float b, float c, float d) {
		addA(a);
		addB(b);
		addC(c);
		addD(d);
		return this;
	}

	public default MutableQuaternion add(Quaternion quaternion) {
		return add(quaternion.a(), quaternion.b(), quaternion.c(), quaternion.d());
	}

	public default MutableVector3D asEulerAngles() {
		// Optimizable
		return MutableVector3D.of(x -> {
					var eulerAngles = Quaternion.toEulerAngles(asImmutable());
					set(Quaternion.fromEulerAngles(Vector3D.of(x, eulerAngles.y(), eulerAngles.z())));
				}, y -> {
					var eulerAngles = Quaternion.toEulerAngles(asImmutable());
					set(Quaternion.fromEulerAngles(Vector3D.of(eulerAngles.x(), y, eulerAngles.z())));
				}, z -> {
					var eulerAngles = Quaternion.toEulerAngles(asImmutable());
					set(Quaternion.fromEulerAngles(Vector3D.of(eulerAngles.x(), eulerAngles.y(), z)));
				},
				() -> Quaternion.toEulerAngles(asImmutable()).x(),
				() -> Quaternion.toEulerAngles(asImmutable()).y(),
				() -> Quaternion.toEulerAngles(asImmutable()).z());
	}
}
