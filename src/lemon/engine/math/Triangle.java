package lemon.engine.math;

import lemon.evolution.pool.VectorPool;

public class Triangle extends VectorArray {
	private Vector3D normal;
	private float area;
	public Triangle() {
		super(3);
	}
	public Triangle(Vector3D a, Vector3D b, Vector3D c) {
		super(a, b, c);
		try (var temp = VectorPool.of(c, v -> v.subtract(a))) {
			this.normal = b.copy().subtract(a).crossProduct(temp);
			float magnitude = normal.getAbsoluteValue();
			this.area = 0.5f * magnitude;
			if (magnitude > 0f) {
				this.normal.divide(magnitude);
			}
		}
	}
	public Vector3D getVertex1() {
		return this.get(0);
	}
	public Vector3D getVertex2() {
		return this.get(1);
	}
	public Vector3D getVertex3() {
		return this.get(2);
	}
	public Vector3D getNormal() {
		return normal;
	}
	public float getArea() {
		return area;
	}

	public boolean isInside(Vector3D point) {
		try (var e10 = VectorPool.of(this.getVertex2(), v -> v.subtract(this.getVertex1()));
			 var e20 = VectorPool.of(this.getVertex3(), v -> v.subtract(this.getVertex1()));
			 var vp = VectorPool.of(point, v -> v.subtract(this.getVertex1()))) {
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
}
