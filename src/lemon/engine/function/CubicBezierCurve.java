package lemon.engine.function;

import java.util.function.Function;

import lemon.engine.math.Vector;

public class CubicBezierCurve implements Function<Float, Vector> {
	private Vector a;
	private Vector b;
	private Vector c;
	private Vector d;
	public CubicBezierCurve(Vector a, Vector b, Vector c, Vector d){
		if(!(a.getDimensions()==b.getDimensions()&&b.getDimensions()==c.getDimensions()&&c.getDimensions()==d.getDimensions())){
			throw new IllegalArgumentException("Dimensions are not equal");
		}
		this.a = a;
		this.b = b;
		this.c = c;
		this.d = d;
	}
	public Vector solve(int index, float n){
		//Coefficients
		float a = this.a.get(index);
		float b = this.b.get(index);
		float c = this.c.get(index);
		float d = this.d.get(index);
		float w = (-a+3*b-3*c+d);
		float x = (3*a-6*b+3*c);
		float y = (-3*a+3*b);
		float z = a-n;
		return Vector.trimValues(CubicEquation.INSTANCE.apply(new Vector(w, x, y, z)), 0.0f, 1.0f);
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
