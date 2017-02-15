package lemon.evolution;

import lemon.engine.function.CubicBezierCurve;
import lemon.engine.math.Vector;

public class BezierCurves {
	public static final Vector START = new Vector(0, 0);
	public static final Vector END = new Vector(1, 1);
	public static final CubicBezierCurve EASE = new CubicBezierCurve(START, new Vector(0.25f, 0.1f), new Vector(0.25f, 1f), END);
	public static final CubicBezierCurve LINEAR = new CubicBezierCurve(START, new Vector(0f, 0f), new Vector(1f, 1f), END);
	public static final CubicBezierCurve EASE_IN = new CubicBezierCurve(START, new Vector(0.42f, 0f), new Vector(1f, 1f), END);
	public static final CubicBezierCurve EASE_OUT = new CubicBezierCurve(START, new Vector(0f, 0f), new Vector(0.58f, 1f), END);
	public static final CubicBezierCurve EASE_IN_OUT = new CubicBezierCurve(START, new Vector(0.42f, 0f), new Vector(0.58f, 1f), END);
	public static final CubicBezierCurve BOUNCE = new CubicBezierCurve(START, new Vector(0.87f, -0.41f), new Vector(0.19f, 1.44f), END);
}