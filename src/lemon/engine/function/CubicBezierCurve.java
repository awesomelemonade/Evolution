package lemon.engine.function;

import java.util.function.Function;

import lemon.engine.math.Vector;

public class CubicBezierCurve implements Function<Float, Vector> {
	private Vector a;
	private Vector b;
	private Vector c;
	private Vector d;
	public CubicBezierCurve(Vector a, Vector b, Vector c, Vector d){
		this.a = a;
		this.b = b;
		this.c = c;
		this.d = d;
	}
	@Override
	public Vector apply(Float t) {
		return a.multiply((float)Math.pow(1-t, 3)).add(b.multiply((float)(3*Math.pow(1-t, 2)*t))).
				add(c.multiply((float)(3*(1-t)*Math.pow(t, 2))).add(d.multiply((float)Math.pow(t, 3))));
	}
	public Vector getA(){
		return a;
	}
	public Vector getB(){
		return b;
	}
	public Vector getC(){
		return c;
	}
	public Vector getD(){
		return d;
	}
}
