package lemon.engine.function;

import lemon.engine.math.Vector2D;

import java.util.function.Function;

public class CubicBezierCurve implements Function<Float, Vector2D> {
	private Vector2D a;
	private Vector2D b;
	private Vector2D c;
	private Vector2D d;

	public CubicBezierCurve(Vector2D a, Vector2D b, Vector2D c, Vector2D d) {
		this.a = a;
		this.b = b;
		this.c = c;
		this.d = d;
	}

	@Override
	public Vector2D apply(Float t) {
		// Warning: Not updated for new Vector
		return a.multiply((float) Math.pow(1 - t, 3)).add(b.multiply((float) (3 * Math.pow(1 - t, 2) * t)))
				.add(c.multiply((float) (3 * (1 - t) * Math.pow(t, 2))).add(d.multiply((float) Math.pow(t, 3))));
	}

	public Vector2D getA() {
		return a;
	}

	public Vector2D getB() {
		return b;
	}

	public Vector2D getC() {
		return c;
	}

	public Vector2D getD() {
		return d;
	}
}
