package lemon.engine.math;

public record Triangle(Vector3D a, Vector3D b, Vector3D c, Vector3D normal, float area) {
	private Triangle(Vector3D a, Vector3D b, Vector3D c, DerivedTriangleData data) {
		this(a, b, c, data.normal, data.area);
	}
	public Triangle(Vector3D a, Vector3D b, Vector3D c) {
		this(a, b, c, calculateDerivedData(a, b, c));
	}

	private static DerivedTriangleData calculateDerivedData(Vector3D a, Vector3D b, Vector3D c) {
		var normal = b.subtract(a).crossProduct(c.subtract(a));
		var magnitude = normal.length();
		if (magnitude > 0f) {
			normal = normal.divide(magnitude);
		}
		return new DerivedTriangleData(normal, 0.5f * magnitude);
	}

	public boolean isInside(Vector3D point) {
		var e10 = b.subtract(a);
		var e20 = c.subtract(a);
		var vp = point.subtract(a);
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

	private record DerivedTriangleData(Vector3D normal, float area) {}
}
