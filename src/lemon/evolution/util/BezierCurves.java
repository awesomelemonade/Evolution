package lemon.evolution.util;

import lemon.engine.function.CubicBezierCurve;
import lemon.engine.math.Vector2D;

public class BezierCurves {
	public static final Vector2D START = Vector2D.of(0, 0);
	public static final Vector2D END = Vector2D.of(1, 1);
	public static final CubicBezierCurve EASE =
			new CubicBezierCurve(START, Vector2D.of(0.25f, 0.1f), Vector2D.of(0.25f, 1f), END);
	public static final CubicBezierCurve LINEAR =
			new CubicBezierCurve(START, Vector2D.of(0f, 0f), Vector2D.of(1f, 1f), END);
	public static final CubicBezierCurve EASE_IN =
			new CubicBezierCurve(START, Vector2D.of(0.42f, 0f), Vector2D.of(1f, 1f), END);
	public static final CubicBezierCurve EASE_OUT =
			new CubicBezierCurve(START, Vector2D.of(0f, 0f), Vector2D.of(0.58f, 1f), END);
	public static final CubicBezierCurve EASE_IN_OUT =
			new CubicBezierCurve(START, Vector2D.of(0.42f, 0f), Vector2D.of(0.58f, 1f), END);
	public static final CubicBezierCurve BOUNCE =
			new CubicBezierCurve(START, Vector2D.of(0.87f, -0.41f), Vector2D.of(0.19f, 1.44f), END);
}