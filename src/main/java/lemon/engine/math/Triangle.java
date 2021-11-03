package lemon.engine.math;

public interface Triangle {
	public static Triangle of(Vector3D a, Vector3D b, Vector3D c) {
		return new ConstantTriangle(a, b, c);
	}

	public record ConstantTriangle(Vector3D a, Vector3D b, Vector3D c, Vector3D normal,
								   float area) implements Triangle {
		public ConstantTriangle(Vector3D a, Vector3D b, Vector3D c, DerivedTriangleData data) {
			this(a, b, c, data.normal, data.area);
		}

		public ConstantTriangle(Vector3D a, Vector3D b, Vector3D c) {
			this(a, b, c, DerivedTriangleData.ofTriangle(a, b, c));
		}
	}

	public record DerivedTriangleData(Vector3D normal, float area) {
		public static DerivedTriangleData ofTriangle(Vector3D a, Vector3D b, Vector3D c) {
			var normal = b.subtract(a).crossProduct(c.subtract(a));
			var magnitude = normal.length();
			if (magnitude > 0f) {
				normal = normal.divide(magnitude);
			}
			return new DerivedTriangleData(normal, 0.5f * magnitude);
		}
	}

	public Vector3D a();

	public Vector3D b();

	public Vector3D c();

	public default Vector3D normal() {
		var normal = b().subtract(a()).crossProduct(c().subtract(a()));
		var magnitude = normal.length();
		if (magnitude > 0f) {
			normal = normal.divide(magnitude);
		}
		return normal;
	}

	public default float area() {
		var normal = b().subtract(a()).crossProduct(c().subtract(a()));
		return 0.5f * normal.length();
	}

	public default boolean isInside(Vector3D point) {
		var vertexA = a();
		var vertexB = b();
		var vertexC = c();
		var e10 = vertexB.subtract(vertexA);
		var e20 = vertexC.subtract(vertexA);
		var vp = point.subtract(vertexA);
		float a = e10.dotProduct(e10);
		float b = e10.dotProduct(e20);
		float c = e20.dotProduct(e20);
		float ac_bb = (a * c) - (b * b);

		float d = vp.dotProduct(e10);
		float e = vp.dotProduct(e20);
		float x = (d * c) - (e * b);
		float y = (e * a) - (d * b);
		float z = x + y - ac_bb;

		// Equivalent to z < 0 && x >= 0 && y >= 0
		return ((Float.floatToRawIntBits(z) & ~(Float.floatToRawIntBits(x) | Float.floatToRawIntBits(y))) & 0x80000000) != 0;
	}
}
